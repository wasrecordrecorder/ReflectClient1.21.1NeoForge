package com.dsp.main.Functions.Combat.Aura;

import com.dsp.main.Functions.Combat.Aura.impl.AttackHandler;
import com.dsp.main.Functions.Combat.Aura.impl.Rotation.*;
import com.dsp.main.Functions.Combat.Aura.impl.RotationAngle;
import com.dsp.main.Functions.Combat.Aura.impl.Snap.SnapFullRotation;
import com.dsp.main.Functions.Combat.Aura.impl.Snap.SnapRotationFov;
import com.dsp.main.Functions.Combat.Aura.impl.TargetSelector;
import com.dsp.main.Core.Event.MoveInputEvent;
import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Core.Other.FreeLook;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.MultiCheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import com.dsp.main.Utils.Minecraft.Client.MoveUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.Arrays;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Combat.AutoFlipFireball.isFlippingFireball;
import static com.dsp.main.Main.isDetect;
import static com.dsp.main.Core.Other.FreeLook.getCameraYaw;
import static com.dsp.main.Utils.Minecraft.Client.InvUtil.requestFreeLook;

public class Aura extends Module {
    public static Entity Target;
    public final Mode componentMode;
    public static final Mode attackType = new Mode("Attack Logic", "1.9", "1.8");
    public static final Mode resetSprintMode = new Mode("Reset Sprint", "Packet + Slow", "Slow", "Packet", "Legit", "Legit + Packet", "None");
    public final Slider OneAndEitCd = new Slider("Click Cd", 1, 5, 1, 1).setVisible(() -> attackType.isMode("1.8"));
    public final Slider attackRange = new Slider("Attack Distance", 3F, 6F, 3F, 0.1F);
    public final Slider fov;
    public final Slider snapSpeed;
    public final Slider aimRange = new Slider("Aim Range", 1F, 9F, 6F, 1F);
    public final CheckBox onlyCrits;
    public final MultiCheckBox checks;
    public final Mode MoveCorrectionMode;
    public final Mode sortMode = new Mode("Sort Mode", "Adapt", "Distance", "Health");
    public final CheckBox throughWalls = new CheckBox("Trough Walls", true);
    public final MultiCheckBox targets = new MultiCheckBox("Targets", Arrays.asList(
            new CheckBox("Players", true),
            new CheckBox("Invisible", true),
            new CheckBox("No Armor", true),
            new CheckBox("Friends", true),
            new CheckBox("Animals", false),
            new CheckBox("Mobs", false)
    ));

    public Aura() {
        super("Aura", 0, Category.COMBAT, "Automatically attacking entities");
        RotationAngle.registerRotation("FunTime", new FunTime());
        RotationAngle.registerRotation("Smooth", new SmoothRotation());
        RotationAngle.registerRotation("Grim", new GrimRotation());
        RotationAngle.registerRotation("Intave", new IntaveRotation());
        RotationAngle.registerRotation("Fast", new AdaptiveRotation());
        RotationAngle.registerRotation("Snap", new SnapFullRotation());
        RotationAngle.registerRotation("Fov Snap", new SnapRotationFov());
        RotationAngle.registerRotation("Universal", new UniversalRotation());
        componentMode = new Mode("Rotation Type", RotationAngle.getRotationNames());
        fov = new Slider("Aura Fov", 40, 120, 60, 5).setVisible(() -> componentMode.getMode().contains("Fov Snap"));
        snapSpeed = new Slider("Snap Speed", 5, 150, 50, 5).setVisible(() -> componentMode.getMode().contains("Snap"));

        checks = new MultiCheckBox("Other", Arrays.asList(
                new CheckBox("Don't hit when you eat", false),
                new CheckBox("Hit only weapon", false),
                new CheckBox("Disable after death", true),
                new CheckBox("Move correction", true),
                new CheckBox("Disabling shield", false),
                new CheckBox("Break Shield", false),
                new CheckBox("Smart Crits", false)
        ));

        onlyCrits = new CheckBox("Only Crits", true).setVisible(() ->
                !attackType.isMode("1.8") && !checks.isOptionEnabled("Smart Crits")
        );

        MoveCorrectionMode = new Mode("Correction Mode", "Free", "Focus").setVisible(() -> checks.isOptionEnabled("Move correction"));

        addSettings(componentMode, attackType, resetSprintMode, OneAndEitCd, attackRange,
                aimRange, fov, snapSpeed, onlyCrits, checks, MoveCorrectionMode, sortMode, throughWalls, targets
        );
    }

    @SubscribeEvent
    public void onRenderFrame(RenderFrameEvent.Pre event) {
        if (isDetect || mc.player == null || mc.level == null) return;
        if (Target == null) {
            Target = TargetSelector.findTarget(this);
        }
        if (Target != null) {
            if (!FreeLook.isFreeLookEnabled) {
                FreeLook.enableFreeLook();
            }
            RotationAngle rotation = RotationAngle.getRotation(componentMode.getMode());
            if (rotation != null && !isFlippingFireball && !requestFreeLook) {
                rotation.update(this, Target);
            }
        } else if (FreeLook.isFreeLookEnabled) {
            FreeLook.disableFreeLook();
        }
        if (Target != null && (mc.player.distanceTo(Target) > aimRange.getValue() || !Target.isAlive())) {
            Target = null;
        }
        if (checks.isOptionEnabled("Disable after death") && mc.player.getHealth() <= 0) {
            this.toggle();
        }
    }

    @SubscribeEvent
    public void OnMoveInput(MoveInputEvent event) {
        if (FreeLook.isFreeLookEnabled && checks.isOptionEnabled("Move correction") && Target != null) {
            if (MoveCorrectionMode.isMode("Free")) {
                MoveUtil.fixMovement(event, getCameraYaw());
            } else if (MoveCorrectionMode.isMode("Focus")) {
                Vec3 playerPos = mc.player.getEyePosition();
                Vec3 targetPos = Target.position().add(0, Target.getBbHeight() * 0.5, 0);
                double deltaX = targetPos.x - playerPos.x;
                double deltaZ = targetPos.z - playerPos.z;
                float targetYaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90F;
                targetYaw = Mth.wrapDegrees(targetYaw);
                MoveUtil.fixMovement(event, targetYaw);
            }
        }
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
        if (Target != null && mc.player != null && !componentMode.getMode().contains("Snap")) {
            AttackHandler.update(this, true);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null) return;
        Target = null;
        if (FreeLook.isFreeLookEnabled) FreeLook.disableFreeLook();
    }
}