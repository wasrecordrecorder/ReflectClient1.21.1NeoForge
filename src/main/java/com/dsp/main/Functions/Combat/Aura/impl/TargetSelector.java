package com.dsp.main.Functions.Combat.Aura.impl;

import com.dsp.main.Functions.Combat.AntiBot;
import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Managers.FrndSys.FriendManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.dsp.main.Utils.Minecraft.Client.ClientPlayerUtil.getHealthFromScoreboard;

public class TargetSelector {
    public static LivingEntity findTarget(Aura aura) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return null;

        float aimRange = aura.aimRange.getValueFloat();
        AABB aabb = new AABB(
                player.position().subtract(aimRange, aimRange, aimRange),
                player.position().add(aimRange, aimRange, aimRange)
        );

        List<Entity> entities = player.level()
                .getEntities((Entity) null, aabb, e -> e instanceof LivingEntity && e != player && e.isAlive() && e.distanceToSqr(player) <= aimRange * aimRange);

        List<LivingEntity> potentialTargets = entities.stream()
                .map(e -> (LivingEntity) e)
                .filter(e -> shouldTarget(e, aura))
                .filter(e -> !AntiBot.isBot(e.getUUID()))
                .filter(e -> !(e instanceof ArmorStand))
                .filter(e -> aura.throughWalls.isEnabled() || player.hasLineOfSight(e))
                .collect(Collectors.toList());

        if (aura.sortMode.isMode("Distance")) {
            potentialTargets.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
        } else if (aura.sortMode.isMode("Health")) {
            potentialTargets.sort(Comparator.comparingDouble(e -> getHealthFromScoreboard(e)[0]));
        } else if (aura.sortMode.isMode("Adapt")) {
            potentialTargets.sort(Comparator.comparingDouble(entity -> {
                double score = 0.0;
                double distance = entity.distanceToSqr(player);
                score += distance * 0.5;
                double health = getHealthFromScoreboard(entity)[0];
                score += health * 10.0;
                boolean hasNoArmor = hasNoArmor(entity);
                if (aura.targets.isOptionEnabled("No Armor") && hasNoArmor) {
                    score += 10.0;
                }
                switch (entity) {
                    case Player player1 when aura.targets.isOptionEnabled("Players") -> score -= 30.0;
                    case Mob mob when aura.targets.isOptionEnabled("Mobs") -> score -= 10.0;
                    case Animal animal when aura.targets.isOptionEnabled("Animals") -> score -= 5.0;
                    default -> {
                    }
                }
                return score;
            }));
        }

        return potentialTargets.isEmpty() ? null : potentialTargets.get(0);
    }

    private static boolean shouldTarget(LivingEntity entity, Aura aura) {
        boolean isPlayer = entity instanceof Player;
        boolean isAnimal = entity instanceof Animal;
        boolean isMob = entity instanceof Mob;
        boolean isInvisible = entity.isInvisible();
        boolean hasNoArmor = hasNoArmor(entity);
        boolean isFriend = FriendManager.isFriend(entity.getName().getString());

        if (aura.targets.isOptionEnabled("Players")) {
            if (isPlayer && (!isFriend || aura.targets.isOptionEnabled("Friends"))) {
                return aura.targets.isOptionEnabled("No Armor") || !hasNoArmor;
            }
        }
        if (!aura.targets.isOptionEnabled("No Armor") && hasNoArmor) return false;
        if (aura.targets.isOptionEnabled("Animals")) {
            if (isAnimal) {
                return true;
            }
        }
        if (aura.targets.isOptionEnabled("Mobs")) {
            if (isMob) {
                return true;
            }
        }
        if (aura.targets.isOptionEnabled("Invisible")) {
            if (isInvisible) {
                return true;
            }
        }
        if (aura.targets.isOptionEnabled("Friends")) {
            if (isFriend) {
                return true;
            }
        }
        if (aura.targets.isOptionEnabled("No Armor")) {
            if (hasNoArmor) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasNoArmor(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.getInventory().armor.stream().allMatch(stack -> stack.isEmpty());
        }
        return false;
    }
}