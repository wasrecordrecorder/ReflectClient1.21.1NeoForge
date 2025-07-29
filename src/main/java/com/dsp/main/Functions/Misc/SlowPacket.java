package com.dsp.main.Functions.Misc;

import com.dsp.main.Core.Event.ClientPacketSendEvent;
import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import net.minecraft.network.protocol.Packet;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.BIKO_FONT;

public class SlowPacket extends Module {
    private final CheckBox vern  = new CheckBox("Back Packet on disable", true);
    private final Slider delay = new Slider("Delay", 1, 100, 1, 1);
    private final ConcurrentLinkedQueue<DelayedPacket> queue = new ConcurrentLinkedQueue<>();

    public SlowPacket() {
        super("SlowPacket", 0, Category.MISC, "Slowing all packets");
        addSettings(delay,vern);
    }
    private final ThreadLocal<Boolean> isSendingDelayed = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public void onSend(ClientPacketSendEvent e) {
        if (mc.player == null) return;
        if (isSendingDelayed.get()) return;
        long sendAt = System.currentTimeMillis() + (long) (delay.getValue() * 100);
        queue.add(new DelayedPacket(e.getPacket(), sendAt));
        e.setCanceled(true);
    }

    @SubscribeEvent
    public void onClientTick(OnUpdate e) {
        if (mc.player == null) return;
        while (true) {
            DelayedPacket dp = queue.peek();
            if (dp == null || System.currentTimeMillis() < dp.sendAt) break;
            queue.poll();
            isSendingDelayed.set(true);
            mc.player.connection.send((Packet<?>) dp.packet);
            isSendingDelayed.set(false);
        }
    }
    private record DelayedPacket(Object packet, long sendAt) {}

    @Override
    public void onDisable() {
        super.onDisable();
        for (int i = 0; i < queue.size(); i++) {
            if (vern.isEnabled()) break;
            DelayedPacket dp = queue.peek();
            queue.poll();
            mc.player.connection.send((Packet<?>) dp.packet);
        }
        queue.clear();
    }
    @SubscribeEvent
    public void onRender(RenderGuiEvent.Pre e) {
        BuiltText text = Builder.text()
                .font(BIKO_FONT.get())
                .text(String.valueOf(queue.size()))
                .color(Color.WHITE)
                .size(7f)
                .thickness(0.05f)
                .build();
        text.render(e.getGuiGraphics().pose().last().pose(), 500, 300);
    }
}