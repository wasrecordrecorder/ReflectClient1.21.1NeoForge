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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Arrays;

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
            new CheckBox("Only Crit", false)
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

    private void performAttack(Player player) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }

        EntityHitResult entityHitResult = (EntityHitResult) hitResult;
        Entity target = entityHitResult.getEntity();
        if (!(target instanceof LivingEntity)) {
            return;
        }

        // Проверяем, соответствует ли цель выбранным опциям
        boolean shouldAttack = false;
        if (Targets.isOptionEnabled("Friends") && FriendManager.isFriend(target.getName().getString())) {
            shouldAttack = true;
        }
        if (Targets.isOptionEnabled("Invisible") && isInvisible(target)) {
            shouldAttack = true;
        }
        if (Targets.isOptionEnabled("No Armor") && hasNoArmor(target)) {
            shouldAttack = true;
        }
        if (!Targets.isOptionEnabled("No Armor") && hasNoArmor(target)) {
            shouldAttack = false;
        }
        if (Targets.isOptionEnabled("Animals") && isAnimal(target)) {
            shouldAttack = true;
        }
        if (Targets.isOptionEnabled("Monsters") && isMonster(target)) {
            shouldAttack = true;
        }
        if (Targets.isOptionEnabled("Players") && target instanceof Player) {
            shouldAttack = true;
        }
        // По умолчанию: атакуем животных и монстров, но не игроков
        if (!Targets.hasAnyEnabled()) {
            shouldAttack = false;
        }

        // Проверяем дистанцию и опции
        if (shouldAttack && player.distanceTo(target) <= attackDistance.getValue() + 0.1) {
            // Проверяем опцию "Only Crit" или откат оружия
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