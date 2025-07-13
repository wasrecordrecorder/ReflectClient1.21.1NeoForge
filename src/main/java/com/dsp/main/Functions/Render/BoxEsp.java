
package com.dsp.main.Functions.Render;

import com.dsp.main.Managers.FrndSys.FriendManager;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.Mode;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Render.ColorUtil;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector4f;

import java.awt.*;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Utils.Render.ColorUtil.getColor;
import static com.dsp.main.Utils.Render.ColorUtil.getColor2;

public class BoxEsp extends Module {

    Mode tools = new Mode("EspMode", "Box", "Effect Glow");

    public BoxEsp() {
        super("Esp", 0, Category.RENDER, "Draw Esp box around player");
        addSetting(tools);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.level != null) {
            List<AbstractClientPlayer> players = mc.level.players();
            for (Player entity : players) {
                if (entity.equals(mc.player)) continue;
                entity.setGlowingTag(false);
            }
        }
    }

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (mc.level == null || mc.player == null || event.getCamera() == null) return;

        // Настройки рендеринга
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.lineWidth(1.5f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        List<AbstractClientPlayer> players = mc.level.players();
        for (Player entity : players) {
            if (entity.equals(mc.player)) continue;
            if (tools.isMode("Box")) {
                entity.setGlowingTag(false);
                Vector4f colors = new Vector4f(getColor(180, 1), getColor(90, 1), getColor(180, 1), getColor(270, 1));
                Vector4f friendColors = new Vector4f(getColor2(180, 1), getColor2(90, 1), getColor2(180, 1), getColor2(270, 1));
                int boxColor = ThemesUtil.getCurrentStyle().getColorLowSpeed(0);
                DrawHelper.drawBox(entity.getX() - 0.5f, entity.getY() - 0.5f, entity.getZ() + 0.5f, entity.getBbHeight() + 0.5f, 2, ColorUtil.rgba(0, 0, 0, 128));
                DrawHelper.drawBoxTest(entity.getX(), entity.getY(), entity.getZ(), entity.getBbHeight(), 1,
                        FriendManager.isFriend(entity.getName().getString()) ? friendColors : colors);
            } else if (tools.isMode("Effect Glow")) {
                entity.setGlowingTag(true);
            } else {
                entity.setGlowingTag(false);
            }
        }

        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}