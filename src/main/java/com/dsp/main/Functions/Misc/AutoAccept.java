package com.dsp.main.Functions.Misc;


import com.dsp.main.Core.FrndSys.FriendManager;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import static com.dsp.main.Api.mc;

public class AutoAccept extends Module {

    public static CheckBox isFrndOnly = new CheckBox("Only Friends", true);

    public AutoAccept() {
        super("Auto Accept", 0, Module.Category.MISC, "Automatically accepting selected actions");
        addSetting(isFrndOnly);
    }

    @SubscribeEvent
    public void OnChatMessage(ClientChatReceivedEvent event) {
        if (mc.player == null) return;
        String message = event.getMessage().getString().toLowerCase();
        if (message.contains("телепортироваться") ||
                message.contains("has requested teleport") ||
                message.contains("просит к вам телепортироваться")) {
            if (isFrndOnly.isEnabled()) {
                for (String friend : FriendManager.friendStorage.getFriends()) {
                    if (message.contains(friend.toLowerCase())) {
                        mc.getConnection().sendCommand("tpaccept");
                    }
                }
            } else {
                mc.getConnection().sendCommand("tpaccept");
            }
        }
    }
}
