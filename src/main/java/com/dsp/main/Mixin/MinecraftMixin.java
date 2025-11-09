package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Player.NoDelay;
import com.dsp.main.Core.Event.OnUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.isDetect;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    private int rightClickDelay;
    @Inject(method = "createTitle", at = @At("RETURN"), cancellable = true)
    private void onTitleCreating(CallbackInfoReturnable<String> cir) {
        if (!isDetect) {
            cir.setReturnValue("Reflect Client 3.3 - Release");
        }
    }
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {
        OnUpdate event = new OnUpdate();
        NeoForge.EVENT_BUS.post(event);
        if (!isDetect && mc.player != null && (
                (Api.isEnabled("FastExp") && mc.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE)
                || (Api.isEnabled("NoDelay") && NoDelay.Options.isOptionEnabled("Place Blocks") && mc.player.getMainHandItem().getItem() instanceof BlockItem)
                        || (mc.player.getMainHandItem().get(DataComponents.FOOD) != null)
        )) {
            this.rightClickDelay = 1;
        }
    }

    @Shadow @Nullable public Screen screen;

    //    @Inject(
//            method = "handleKeybinds",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void redirectScreenField(CallbackInfo ci) {
//        if (Api.isEnabled("ScreenWalk")) ci.cancel();
//    }
    @Inject(
            method = "handleKeybinds()V",
            at = @At("HEAD")
    )
    private void onHandleKeybinds(CallbackInfo ci) {
        if (Api.isEnabled("ScreenWalk")) {
            this.screen = null;
        }
    }
}