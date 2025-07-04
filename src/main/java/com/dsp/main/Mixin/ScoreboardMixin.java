package com.dsp.main.Mixin; // Замените на ваш пакет

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin {
    private static final Logger LOGGER = LogManager.getLogger("ScoreboardMixin");

    @Final
    @Shadow
    private final Object2ObjectMap<String, PlayerTeam> teamsByPlayer = new Object2ObjectOpenHashMap<>();

    @Shadow
    public abstract PlayerTeam getPlayersTeam(String username);

    /**
     * @author was_record
     * @reason Replace vanilla removePlayerFromTeam(String) with custom logic to prevent crashes on servers like ReallyWorld
     */
    @Overwrite
    public boolean removePlayerFromTeam(String playerName) {
        PlayerTeam playerTeam = this.getPlayersTeam(playerName);
        if (playerTeam != null) {
            PlayerTeam currentTeam = this.getPlayersTeam(playerName);
            if (currentTeam != playerTeam) {
                LOGGER.warn("Попытка удалить игрока {} из команды {}, но он находится в команде {}. Пропускаем удаление.",
                        playerName, playerTeam.getName(), currentTeam != null ? currentTeam.getName() : "null");
                return false;
            }
            this.teamsByPlayer.remove(playerName);
            playerTeam.getPlayers().remove(playerName);
            return true;
        }
        return false;
    }

    /**
     * @author was_record
     * @reason Replace vanilla removePlayerFromTeam(String, PlayerTeam) with custom logic to prevent crashes on servers like ReallyWorld
     */
    @Overwrite
    public void removePlayerFromTeam(String pUsername, PlayerTeam pPlayerTeam) {
        PlayerTeam currentTeam = this.getPlayersTeam(pUsername);
        if (currentTeam != pPlayerTeam) {
            LOGGER.warn("Попытка удалить игрока {} из команды {}, но он находится в команде {}. Пропускаем удаление.",
                    pUsername, pPlayerTeam.getName(), currentTeam != null ? currentTeam.getName() : "null");
            return;
        }
        this.teamsByPlayer.remove(pUsername);
        pPlayerTeam.getPlayers().remove(pUsername);
    }
}