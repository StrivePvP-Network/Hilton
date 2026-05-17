package net.frozenorb.foxtrot.gameplay.events.koth.commands.koth;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.frozenorb.foxtrot.gameplay.events.Event;
import net.frozenorb.foxtrot.gameplay.events.EventType;
import net.frozenorb.foxtrot.gameplay.events.koth.KOTH;
import cc.fyre.proton.command.Command;
import cc.fyre.proton.command.param.Parameter;

public class KOTHLootCommand {

    @Command(names={ "KOTH Loot", "KOTH SetLoot" }, permission="foxtrot.koth.admin")
    public static void kothLoot(Player sender, @Parameter(name="koth") Event koth) {
        if (koth.getType() != EventType.KOTH) {
            sender.sendMessage(ChatColor.RED + "Unable to modify loot for a non-KOTH event.");
        } else {
            KOTH k = (KOTH) koth;
            Inventory inventory = Bukkit.createInventory(null, 27, "KOTH Loot: " + k.getName());

            for (ItemStack item : k.getLoot()) {
                inventory.addItem(item);
            }

            sender.openInventory(inventory);
        }
    }

}
