package com.dsp.main.Functions.Combat;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Mixin.Accesors.LivingEntityAccessor;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.*;
import com.dsp.main.Utils.Minecraft.Client.ClientFallDistance;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

import static com.dsp.main.Api.mc;

public class AutoTotem extends Module {
    private static MultiCheckBox Checks = new MultiCheckBox("Checks", Arrays.asList(
            new CheckBox("Health", true),
            new CheckBox("Crystal", false),
            new CheckBox("Dynamite", false),
            new CheckBox("Fall Distance", false)
    ));
    public static Slider health = new Slider("Health", 1, 20, 6, 1).setVisible(() -> Checks.isOptionEnabled("Health"));
    private static CheckBox saveEnchantedTotem = new CheckBox("Save Talismans", false);
    private static CheckBox SwapBack = new CheckBox("Swap Item Back", false);

    private static final InvUtil invUtil = new InvUtil();
    private Status status = Status.OFFLINE;
    private ItemStack previousOffhandItem = ItemStack.EMPTY;

    public AutoTotem() {
        super("AutoTotem", 0, Category.COMBAT, "Automatically equips totem to avoid your death");
        addSettings(Checks, health, saveEnchantedTotem, SwapBack);
    }

    public enum Status {
        OFFLINE,
        SAVING
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
        if (mc.player == null) return;
        ItemStack offhand = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        boolean offIsTotem = offhand.getItem() == Items.TOTEM_OF_UNDYING;
        boolean offIsEnchanted = offhand.isEnchanted() && saveEnchantedTotem.isEnabled();
        boolean hasTotem = offIsTotem && !offIsEnchanted;

        if (CheckDanger() && !hasTotem && findTotemSlot() != -1) {
            if (previousOffhandItem.isEmpty()) {
                previousOffhandItem = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND).copy();
            }
            int totemSlot = findTotemSlot();
            if (totemSlot != -1) {
                invUtil.swapHand(totemSlot, InteractionHand.OFF_HAND);
                status = Status.SAVING;
            }  else {
                status = Status.OFFLINE;
            }
        } else if (!previousOffhandItem.isEmpty() && SwapBack.isEnabled() && findTotemSlot() == -1) {
            int previousItemSlot = invUtil.getSlotWithExactStack(previousOffhandItem);
            if (previousItemSlot != -1) {
                invUtil.swapHand(previousItemSlot, InteractionHand.OFF_HAND);
            }
            status = Status.OFFLINE;
            previousOffhandItem = ItemStack.EMPTY;
        } else if (!CheckDanger() && hasTotem && SwapBack.isEnabled() && !previousOffhandItem.isEmpty()) {
            int previousItemSlot = invUtil.getSlotWithExactStack(previousOffhandItem);
            if (previousItemSlot != -1) {
                invUtil.swapHand(previousItemSlot, InteractionHand.OFF_HAND);
            }
            status = Status.OFFLINE;
            previousOffhandItem = ItemStack.EMPTY;
        } else {
            status = Status.OFFLINE;
        }
    }

    private int findTotemSlot() {
        java.util.List<Integer> normal = new java.util.ArrayList<>();
        java.util.List<Integer> enchanted = new java.util.ArrayList<>();
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {

                if (stack.isEnchanted()) {
                    if (i <= 8) {
                        enchanted.add(i + 36);
                    } else {
                        enchanted.add(i);
                    }
                } else {
                    if (i <= 8) {
                        normal.add(i + 36);
                    } else {
                        normal.add(i);
                    }
                }
            }
        }
        if (saveEnchantedTotem.isEnabled()) {
            if (!normal.isEmpty()) return normal.getFirst();
            if (!enchanted.isEmpty()) return enchanted.getFirst();
        } else {
            if (!normal.isEmpty()) return normal.getFirst();
            if (!enchanted.isEmpty()) return enchanted.getFirst();
        }
        return -1;
    }
    public static boolean CheckDanger() {
        if (mc.player == null || mc.level == null || mc.player.isCreative()) {
            return false;
        }
        float currentHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        Vec3 playerPos = mc.player.position();
        if (Checks.isOptionEnabled("Health")) {
            float healthThreshold = health.getValueFloat();
            if (currentHealth <= healthThreshold) {
                return true;
            }
        }
        if (Checks.isOptionEnabled("Crystal")) {
            double CRYSTAL_DANGER_RADIUS = 6.0;
            double CRYSTAL_DANGER_RADIUS_SQ = CRYSTAL_DANGER_RADIUS * CRYSTAL_DANGER_RADIUS;
            AABB crystalAABB = new AABB(
                    playerPos.x - CRYSTAL_DANGER_RADIUS, playerPos.y - CRYSTAL_DANGER_RADIUS, playerPos.z - CRYSTAL_DANGER_RADIUS,
                    playerPos.x + CRYSTAL_DANGER_RADIUS, playerPos.y + CRYSTAL_DANGER_RADIUS, playerPos.z + CRYSTAL_DANGER_RADIUS
            );
            List<EndCrystal> entities = mc.level.getEntitiesOfClass(EndCrystal.class, crystalAABB);
            for (EndCrystal entity : entities) {
                if (!entity.isRemoved()) {
                    double distanceSq = entity.distanceToSqr(playerPos);
                    if (distanceSq <= CRYSTAL_DANGER_RADIUS_SQ) {
                        double heightDiff = entity.getY() - playerPos.y;
                        if (heightDiff >= -2.0 && heightDiff <= 3.0) {
                            return true;
                        }
                    }
                }
            }
        }
        if (Checks.isOptionEnabled("Dynamite")) {
            double TNT_DANGER_RADIUS = 8.0;
            double TNT_DANGER_RADIUS_SQ = TNT_DANGER_RADIUS * TNT_DANGER_RADIUS;
            AABB tntAABB = new AABB(
                    playerPos.x - TNT_DANGER_RADIUS, playerPos.y - TNT_DANGER_RADIUS, playerPos.z - TNT_DANGER_RADIUS,
                    playerPos.x + TNT_DANGER_RADIUS, playerPos.y + TNT_DANGER_RADIUS, playerPos.z + TNT_DANGER_RADIUS
            );
            List<PrimedTnt> entities = mc.level.getEntitiesOfClass(PrimedTnt.class, tntAABB);
            for (PrimedTnt entity : entities) {
                if (!entity.isRemoved()) {
                    double distanceSq = entity.distanceToSqr(playerPos);
                    if (distanceSq <= TNT_DANGER_RADIUS_SQ) {
                        int fuse = entity.getFuse();
                        if (fuse <= 15) {
                            return true;
                        }
                    }
                }
            }
        }
        if (Checks.isOptionEnabled("Fall Distance")) {
            float fallDistance = ClientFallDistance.get();
            if (fallDistance > 3.0f) {
                float damage = ((LivingEntityAccessor)(Object)mc.player).callCalculateFallDamage(fallDistance, 1.0f);;
                if (damage >= (currentHealth - 3)) {
                    if (!mc.player.isInWater() && !mc.player.isInLava()) {
                        return !mc.player.hasEffect(MobEffects.SLOW_FALLING) &&
                                !mc.player.onClimbable() &&
                                !mc.player.isSpectator() &&
                                !mc.player.isSwimming() &&
                                mc.player.getVehicle() == null;
                    }
                }
            }
            if (mc.player.isFallFlying()) {
                Vec3 lookVec = mc.player.getLookAngle();
                Vec3 checkPos = playerPos.add(lookVec.scale(5));

                AABB forwardBox = new AABB(playerPos, checkPos).inflate(0.3);
                return mc.level.getBlockCollisions(mc.player, forwardBox).iterator().hasNext();
            }
        }
        double CREEPER_DANGER_RADIUS = 14.0;
        AABB creeperAABB = new AABB(
                playerPos.x - CREEPER_DANGER_RADIUS, playerPos.y - CREEPER_DANGER_RADIUS, playerPos.z - CREEPER_DANGER_RADIUS,
                playerPos.x + CREEPER_DANGER_RADIUS, playerPos.y + CREEPER_DANGER_RADIUS, playerPos.z + CREEPER_DANGER_RADIUS
        );
        List<Creeper> creepers = mc.level.getEntitiesOfClass(Creeper.class, creeperAABB);
        for (Creeper creeper : creepers) {
            if (!creeper.isRemoved() && creeper.getSwelling(0) > 0.8) {
                return true;
            }
        }

        return false;
    }
}