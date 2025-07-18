package com.dsp.main.Functions.Combat.Aura;

import com.dsp.main.Functions.Combat.Aura.impl.AttackHandler;
import com.dsp.main.Functions.Combat.Aura.impl.Rotation.*;
import com.dsp.main.Functions.Combat.Aura.impl.RotationAngle;
import com.dsp.main.Functions.Combat.Aura.impl.TargetSelector;
import com.dsp.main.Managers.Event.MoveInputEvent;
import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Managers.FreeLook;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.Mode;
import com.dsp.main.UI.ClickGui.Settings.MultiCheckBox;
import com.dsp.main.UI.ClickGui.Settings.Slider;
import com.dsp.main.Utils.Minecraft.Client.MoveUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.Arrays;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Combat.AutoFlipFireball.isFlippingFireball;
import static com.dsp.main.Main.isDetect;
import static com.dsp.main.Managers.FreeLook.getCameraYaw;

public class Aura extends Module {
    public static Entity Target;
    public final Mode componentMode;
    public static final Mode attackType = new Mode("Attack Logic",
            "1.9", "1.8");
    public static final Mode resetSprintMode = new Mode("Reset Sprint", "Packet + Slow", "Slow", "Packet", "Legit", "Legit + Packet", "None");
    public final Slider OneAndEitCd = new Slider("Click Cd", 1, 5, 1, 1).setVisible(() -> attackType.isMode("1.8"));
    public final Slider attackRange = new Slider("Attack Distance", 3F, 6F, 3F, 0.1F);
    public final Slider aimRange = new Slider("Aim Range", 1F, 9F, 6F, 1F);
    public final CheckBox onlyCrits = new CheckBox("Only Crits", true).setVisible(()-> !attackType.isMode("1.8"));

    public final MultiCheckBox checks = new MultiCheckBox("Other", Arrays.asList(
            new CheckBox("Don't hit when you eat", false),
            new CheckBox("Hit only weapon", false),
            new CheckBox("Disable after death", true),
            new CheckBox("Move correction", true)
    ));
    public final Mode  MoveCorrectionMode = new Mode("Correction Mode", "Free", "Focus").setVisible(() -> checks.isOptionEnabled("Move correction"));
    public final Mode  sortMode = new Mode("Sort Mode", "Adapt", "Distance", "Health");
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
        //RotationAngle.registerRotation("SpookyTime Duels", new SpookyTimeDuels());
        RotationAngle.registerRotation("Grim", new GrimRotation());
        RotationAngle.registerRotation("Intave", new IntaveRotation());
        RotationAngle.registerRotation("Fast", new AdaptiveRotation());
        //RotationAngle.registerRotation("Fast", new AdaptiveRotation());

        componentMode = new Mode("Rotation Type", RotationAngle.getRotationNames());
        addSettings(componentMode,attackType,resetSprintMode, OneAndEitCd, attackRange, aimRange, onlyCrits, checks,MoveCorrectionMode, sortMode, throughWalls, targets);
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
            if (rotation != null && !isFlippingFireball) {
                rotation.update(this, Target);
            }
        } else if (FreeLook.isFreeLookEnabled) {
            FreeLook.disableFreeLook();
        }
        if (Target != null && (mc.player.distanceTo(Target) > aimRange.getValue() || !Target.isAlive())) {
            Target = null;
        }
        if (mc.player.getHealth() <= 0) {
            this.toggle();
        }
    }
    @SubscribeEvent
    public void OnMoveInput(MoveInputEvent event) {
        if (FreeLook.isFreeLookEnabled && checks.isOptionEnabled("Move correction") && Target != null) {
            if (MoveCorrectionMode.isMode("Free")) {
                MoveUtil.fixMovement(event, getCameraYaw());
            } else if (MoveCorrectionMode.isMode("Focus")) {
                MoveUtil.fixMovementFocus(event, (LivingEntity) Target);
            }
        }
    }
    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
        if (!(Target == null) && !(mc.player == null)) {
            AttackHandler.update(this);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null) return;
        if (FreeLook.isFreeLookEnabled) FreeLook.disableFreeLook();
    }
}
