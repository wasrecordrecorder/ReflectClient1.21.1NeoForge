package com.dsp.main.Core.Sound;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SoundRegister {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "dsp");
    public static final Supplier<SoundEvent> BELL = register("bell");
    public static final Supplier<SoundEvent> BONK = register("bonk");
    public static final Supplier<SoundEvent> CRIME = register("crime");
    public static final Supplier<SoundEvent> DISABLE = register("disable");
    public static final Supplier<SoundEvent> ENABLE = register("enable");
    public static final Supplier<SoundEvent> METALLIC = register("metallic");
    public static final Supplier<SoundEvent> OFF = register("off");
    public static final Supplier<SoundEvent> ON = register("on");
    public static final Supplier<SoundEvent> RUST = register("rust");
    public static final Supplier<SoundEvent> WASTED = register("wasted");

    private static Supplier<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("dsp", name)));
    }
}
