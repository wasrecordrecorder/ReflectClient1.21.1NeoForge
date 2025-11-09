package com.dsp.main.Functions.Render;

import com.dsp.main.Module;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.DrawHelper;
import com.dsp.main.Utils.Render.Other.ESPUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.awt.Color;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.BIKO_FONT;

public class TNTTimer extends Module {

    public TNTTimer() {
        super("TNTTimer", 0, Category.RENDER, "Shows time until TNT explosion and damage");
    }

    @SubscribeEvent
    public void onRender2D(RenderGuiEvent.Post event) {
        if (mc.player == null || mc.level == null) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof PrimedTnt tnt) {
                int fuse = tnt.getFuse();
                String timeText = String.format("%.1f seconds", fuse / 20.0f);

                Vec3 tntPos = new Vec3(
                        tnt.getX(),
                        tnt.getY() + tnt.getBbHeight() + 0.5,
                        tnt.getZ()
                );

                Vector3d screenPos = ESPUtils.toScreen(tntPos);
                if (screenPos.z == 0) continue;

                double damage = calculateDamage(tnt);
                boolean lethal = mc.player.getHealth() + mc.player.getAbsorptionAmount() < damage;

                int textWidth = (int) BIKO_FONT.get().getWidth(timeText, 7f);
                float x = (float) screenPos.x - textWidth / 2f;
                float y = (float) screenPos.y - (damage > 0 ? 24 : 12);
                DrawHelper.rectangle(guiGraphics.pose(), x - 2, y, textWidth + 5, 12, 1.5f,
                        new Color(30, 30, 30, 160).getRGB());


                BuiltText text = Builder.text()
                        .font(BIKO_FONT.get())
                        .text(timeText)
                        .color(Color.WHITE)
                        .size(7f)
                        .thickness(0.05f)
                        .build();
                text.render(new Matrix4f(), (int)x, (int)(y + 4));

                if (damage > 0) {
                    String damageText = lethal ? "Danger!" : String.format("%.0f damage", damage);
                    int damageWidth = (int) BIKO_FONT.get().getWidth(damageText, 7f);
                    float damageX = (float) screenPos.x - damageWidth / 2f;
                    float damageY = y + 14;

                    Color damageBg = lethal ? new Color(255, 0, 0, 160) :
                            new Color(30, 30, 30, 160);

                    DrawHelper.rectangle(guiGraphics.pose(), damageX - 2, damageY, damageWidth + 5, 12, 1.5f, damageBg.getRGB());
                    BuiltText textDmg = Builder.text()
                            .font(BIKO_FONT.get())
                            .text(damageText)
                            .color(Color.WHITE)
                            .size(7f)
                            .thickness(0.05f)
                            .build();
                    textDmg.render(new Matrix4f(), (int)damageX, (int)(damageY + 4));
                }
            }
        }
    }

    private double calculateDamage(PrimedTnt tnt) {
        if (mc.level == null || mc.player == null) return 0;

        float explosionSize = 4.0f;
        Vec3 explosionPos = tnt.position();
        double explosionX = explosionPos.x;
        double explosionY = tnt.getY(0.0625);
        double explosionZ = explosionPos.z;

        float radius = explosionSize * 2.0f;
        int minX = Mth.floor(explosionX - radius - 1.0);
        int maxX = Mth.floor(explosionX + radius + 1.0);
        int minY = Mth.floor(explosionY - radius - 1.0);
        int maxY = Mth.floor(explosionY + radius + 1.0);
        int minZ = Mth.floor(explosionZ - radius - 1.0);
        int maxZ = Mth.floor(explosionZ + radius + 1.0);

        AABB searchArea = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        List<Entity> entities = mc.level.getEntities(tnt, searchArea);

        Vec3 explosionVec = new Vec3(explosionX, explosionY, explosionZ);

        for (Entity entity : entities) {
            if (entity != mc.player) continue;

            double distanceToExplosion = Math.sqrt(entity.distanceToSqr(explosionVec));
            double distanceRatio = distanceToExplosion / radius;

            if (distanceRatio <= 1.0) {
                double deltaX = entity.getX() - explosionX;
                double deltaY = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - explosionY;
                double deltaZ = entity.getZ() - explosionZ;
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                if (distance == 0.0) continue;

                deltaX /= distance;
                deltaY /= distance;
                deltaZ /= distance;

                double blockDensity = calculateBlockDensity(explosionVec, entity);
                double impact = (1.0 - distanceRatio) * blockDensity;

                double damage = ((impact * impact + impact) / 2.0 * 7.0 * radius + 1.0);

                return Math.floor(damage);
            }
        }

        return 0;
    }

    private double calculateBlockDensity(Vec3 explosionPos, Entity entity) {
        if (mc.level == null) return 0.0;

        AABB entityBox = entity.getBoundingBox();

        double xSize = entityBox.maxX - entityBox.minX;
        double ySize = entityBox.maxY - entityBox.minY;
        double zSize = entityBox.maxZ - entityBox.minZ;

        double stepX = 1.0 / ((xSize) * 2.0 + 1.0);
        double stepY = 1.0 / ((ySize) * 2.0 + 1.0);
        double stepZ = 1.0 / ((zSize) * 2.0 + 1.0);

        if (stepX < 0 || stepY < 0 || stepZ < 0) return 0.0;

        double offsetX = (1.0 - Math.floor(1.0 / stepX) * stepX) / 2.0;
        double offsetZ = (1.0 - Math.floor(1.0 / stepZ) * stepZ) / 2.0;

        int visiblePoints = 0;
        int totalPoints = 0;

        for (double x = 0.0; x <= 1.0; x += stepX) {
            for (double y = 0.0; y <= 1.0; y += stepY) {
                for (double z = 0.0; z <= 1.0; z += stepZ) {
                    double testX = Mth.lerp(x, entityBox.minX, entityBox.maxX);
                    double testY = Mth.lerp(y, entityBox.minY, entityBox.maxY);
                    double testZ = Mth.lerp(z, entityBox.minZ, entityBox.maxZ);

                    Vec3 testPoint = new Vec3(testX + offsetX, testY, testZ + offsetZ);

                    if (canSeePoint(explosionPos, testPoint)) {
                        visiblePoints++;
                    }

                    totalPoints++;
                }
            }
        }

        if (totalPoints == 0) return 0.0;

        return (double) visiblePoints / totalPoints;
    }

    private boolean canSeePoint(Vec3 from, Vec3 to) {
        if (mc.level == null) return true;

        ClipContext context = new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mc.player
        );

        BlockHitResult result = mc.level.clip(context);

        return result.getType() == HitResult.Type.MISS;
    }
}