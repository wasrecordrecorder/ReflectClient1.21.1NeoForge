package com.dsp.main.Mixin;

import com.dsp.main.Managers.Hooks.ServerboundInteractPacketAccessor;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerboundInteractPacket.class)
public abstract class ServerboundInteractPacketMixin implements ServerboundInteractPacketAccessor {
    @Shadow
    private int entityId;

    @Override
    public int getEntityId() {
        return this.entityId;
    }
}