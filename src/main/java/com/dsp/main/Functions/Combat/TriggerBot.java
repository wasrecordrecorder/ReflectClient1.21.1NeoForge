package com.dsp.main.Functions.Combat;

import com.dsp.main.Managers.FrndSys.FriendManager;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.MultiCheckBox;
import com.dsp.main.UI.ClickGui.Settings.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.isDetect;
import static com.dsp.main.Utils.Minecraft.Client.ClientPlayerUtil.*;

public class TriggerBot extends Module {
    private static Slider attackDistance = new Slider("Attack Distance", 1, 6, 3, 1);
    public static MultiCheckBox Targets = new MultiCheckBox("Targets", Arrays.asList(
            new CheckBox("Friends", false),
            new CheckBox("Invisible", false),
            new CheckBox("No Armor", false),
            new CheckBox("Animals", false),
            new CheckBox("Monsters", false),
            new CheckBox("Players", false)
    ));
    public static MultiCheckBox Options = new MultiCheckBox("Options", Arrays.asList(
            new CheckBox("Don't attack if using item", false),
            new CheckBox("Only Crit", false),
            new CheckBox("Wall Check", false)
    ));

    public TriggerBot() {
        super("Trigger Bot", 0, Category.COMBAT, "Автоматически атакует цель наводки");
        addSettings(attackDistance, Targets, Options);
    }

    private static boolean isInvisible(Entity entity) {
        return entity.isInvisible();
    }

    private static boolean hasNoArmor(Entity entity) {
        if (entity instanceof Player player) {
            for (ItemStack stack : player.getInventory().armor) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isAnimal(Entity entity) {
        return entity instanceof Animal;
    }

    private static boolean isMonster(Entity entity) {
        return entity instanceof Monster;
    }

    private boolean hasClearLineOfSight(Player player, Entity target) {
        Vec3 playerEyePos = player.getEyePosition();
        Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
        ClipContext context = new ClipContext(
                playerEyePos,
                targetPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );
        return mc.level.clip(context).getType() != HitResult.Type.BLOCK;
    }

    private boolean isRayIntersectingAABB(Player player, Entity entity, double maxDistance) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F).scale(maxDistance);
        Vec3 endPos = eyePos.add(lookVec);
        AABB entityBox = entity.getBoundingBox();
        return entityBox.clip(eyePos, endPos).isPresent();
    }

    private Entity findTarget(Player player) {
        double maxDistance = attackDistance.getValue() + 0.1;
        Vec3 eyePos = player.getEyePosition();
        AABB searchBox = new AABB(eyePos, eyePos).inflate(maxDistance);
        List<Entity> entities = mc.level.getEntities(player, searchBox, entity -> entity instanceof LivingEntity && entity.isAlive());

        return entities.stream()
                .filter(entity -> {
                    boolean shouldTarget = false;
                    if (Targets.isOptionEnabled("Friends") && FriendManager.isFriend(entity.getName().getString())) {
                        shouldTarget = true;
                    }
                    if (FriendManager.isFriend(entity.getName().getString())) {
                        return false;
                    }
                    if (Targets.isOptionEnabled("Invisible") && isInvisible(entity)) {
                        shouldTarget = true;
                    }
                    if (Targets.isOptionEnabled("No Armor") && hasNoArmor(entity)) {
                        shouldTarget = true;
                    }
                    if (!Targets.isOptionEnabled("No Armor") && hasNoArmor(entity)) {
                        shouldTarget = false;
                    }
                    if (Targets.isOptionEnabled("Animals") && isAnimal(entity)) {
                        shouldTarget = true;
                    }
                    if (Targets.isOptionEnabled("Monsters") && isMonster(entity)) {
                        shouldTarget = true;
                    }
                    if (Targets.isOptionEnabled("Players") && entity instanceof Player) {
                        shouldTarget = true;
                    }
                    if (!Targets.hasAnyEnabled()) {
                        shouldTarget = false;
                    }
                    if (!shouldTarget) {
                        return false;
                    }
                    if (player.distanceTo(entity) > maxDistance) {
                        return false;
                    }
                    // Check if the player's view ray intersects the entity's hitbox
                    return isRayIntersectingAABB(player, entity, maxDistance);
                })
                .min(Comparator.comparingDouble(player::distanceTo))
                .orElse(null);
    }

    private void performAttack(Player player) {
        Minecraft mc = Minecraft.getInstance();
        Entity target = findTarget(player);
        if (target == null || !(target instanceof LivingEntity)) {
            return;
        }
        if (Options.isOptionEnabled("Wall Check") && !hasClearLineOfSight(player, target)) {
            return;
        }
        if (player.distanceTo(target) <= attackDistance.getValue() + 0.1) {
            if (Options.isOptionEnabled("Only Crit") && !isPlayerFalling()) {
                return;
            }
            if (!Options.isOptionEnabled("Only Crit") && player.getAttackStrengthScale(0.0F) < 1.0F) {
                return;
            }
            if (Options.isOptionEnabled("Don't attack if using item") && mc.player.isUsingItem()) {
                return;
            }
            if (mc.gameMode != null) {
                mc.gameMode.attack(player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(ClientTickEvent.Pre event) {
        if (isDetect || mc.level == null) return;
        if (mc.player.isLocalPlayer()) {
            performAttack(mc.player);
        }
    }
}