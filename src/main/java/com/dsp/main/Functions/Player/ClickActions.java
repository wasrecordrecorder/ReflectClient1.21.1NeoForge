package com.dsp.main.Functions.Player;

import com.dsp.main.Managers.FrndSys.FriendManager;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.BindCheckBox;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import static com.dsp.main.Managers.FrndSys.FriendManager.*;
import static com.dsp.main.Utils.Minecraft.Client.AutoEatUtil.eatItemFromInventory;
import static com.dsp.main.Utils.Minecraft.Client.InventoryUtils.useItemFromInventory;

public class ClickActions extends Module {
    public static CheckBox shiftBp = new CheckBox("Shift Bypass", false);
    public ClickActions() {
        super("Click Actions", 0, Category.PLAYER, "Действия на предметах");
        addSettings(
                shiftBp,
                new BindCheckBox("Throw Pearl", 0, ()->useItemFromInventory(Items.ENDER_PEARL, 8)),
                new BindCheckBox("Click Friend", 0, this::ClickFriend),
                new BindCheckBox("Eat GApple", 0, ()->eatItemFromInventory(Items.GOLDEN_APPLE)),
                new BindCheckBox("Eat Enchanted GApple", 0, ()->eatItemFromInventory(Items.ENCHANTED_GOLDEN_APPLE)),
                new BindCheckBox("Eat Chorus Fruit", 0, ()->eatItemFromInventory(Items.CHORUS_FRUIT)),
                new BindCheckBox("Eat Golden Carrot", 0, ()->eatItemFromInventory(Items.GOLDEN_CARROT)),
                new BindCheckBox("Drink Heal Potion", 0, ()->eatItemFromInventory(Items.POTION))
        );
    }
    private void ClickFriend() {
        Minecraft mc = Minecraft.getInstance();
        HitResult hitResult = mc.hitResult;
        if (hitResult instanceof EntityHitResult entityHitResult) {
            Entity targetEntity = entityHitResult.getEntity();

            if (targetEntity instanceof Player targetPlayer) {
                String targetName = targetPlayer.getName().getString();
                if (FriendManager.isFriend(targetName)) {
                    FriendManager.removeFriend(targetName);
                    ChatUtil.sendMessage("Игрок " + targetName + " удалён из друзей.");
                } else {
                    FriendManager.addFriend(targetName);
                    ChatUtil.sendMessage("Игрок " + targetName + " добавлен в друзья.");
                }
            } else {
                ChatUtil.sendMessage("Наведитесь на игрока для добавления или удаления из друзей.");
            }
        } else {
            ChatUtil.sendMessage("Наведитесь на игрока для добавления или удаления из друзей.");
        }
    }
    @SubscribeEvent
    public void onbldlaw(ClientTickEvent.Pre event) {

    }
}
