package net.frozenorb.foxtrot.gameplay.abilitytrader;

import cc.fyre.proton.command.Command;
import net.frozenorb.foxtrot.Foxtrot;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TraderCommand {

    @Command(names = { "trader spawn" }, permission = "foxtrot.command.trader")
    public static void traderSpawn(Player player) {
        AbilityTraderHandler handler = Foxtrot.getInstance().getMapHandler().getAbilityTraderHandler();
        handler.spawnNPC();
        player.sendMessage(ChatColor.GREEN + "Forced a spawn of the Ability Trader NPC.");
    }

    @Command(names = { "trader despawn" }, permission = "foxtrot.command.trader")
    public static void traderDespawn(Player player) {
        AbilityTraderHandler handler = Foxtrot.getInstance().getMapHandler().getAbilityTraderHandler();
        handler.despawnNPC();
        player.sendMessage(ChatColor.GREEN + "Despawned all Ability Trader NPCs.");
    }

    @Command(names = { "trader tp", "trader teleport" }, permission = "foxtrot.command.trader")
    public static void traderTp(Player player) {
        AbilityTraderHandler handler = Foxtrot.getInstance().getMapHandler().getAbilityTraderHandler();
        if (handler.getActiveNPC() == null || !handler.getActiveNPC().isValid()) {
            player.sendMessage(ChatColor.RED + "There is currently no active Ability Trader NPC spawned!");
            return;
        }

        player.teleport(handler.getActiveNPC().getLocation());
        player.sendMessage(ChatColor.GREEN + "Teleported you to the Ability Trader NPC.");
    }
}
