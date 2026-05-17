package net.frozenorb.foxtrot.gameplay.events.koth.commands.koth;

import cc.fyre.proton.command.Command;
import cc.fyre.proton.command.param.Parameter;
import net.frozenorb.foxtrot.gameplay.events.Event;
import net.frozenorb.foxtrot.gameplay.events.EventType;
import net.frozenorb.foxtrot.gameplay.events.koth.KOTH;
import net.frozenorb.foxtrot.gameplay.events.koth.selection.KOTHSelectionHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class KOTHSetCapzoneCommand {

    @Command(names = { "KOTH setcapzone", "koth setcap" }, permission = "foxtrot.koth.admin")
    public static void setCapzone(Player sender, @Parameter(name = "koth") Event event) {
        if (event.getType() != EventType.KOTH) {
            sender.sendMessage(ChatColor.RED + "This command is only for KOTHs!");
            return;
        }

        KOTH koth = (KOTH) event;
        KOTHSelectionHandler.getSelections().put(sender.getUniqueId(), new KOTHSelectionHandler.Selection(koth));

        ItemStack wand = new ItemStack(Material.GOLD_HOE);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + ChatColor.BOLD.toString() + "Capzone Selection Wand");
        meta.setLore(Arrays.asList(
                ChatColor.YELLOW + "Left Click: " + ChatColor.WHITE + "Set Point 1",
                ChatColor.YELLOW + "Right Click: " + ChatColor.WHITE + "Set Point 2",
                "",
                ChatColor.GRAY + "Selecting for: " + ChatColor.BLUE + koth.getName()));
        wand.setItemMeta(meta);

        sender.getInventory().addItem(wand);
        sender.updateInventory();

        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "KOTH Capzone Selection");
        sender.sendMessage(
                ChatColor.YELLOW + "You are now selecting the capzone for: " + ChatColor.WHITE + koth.getName());
        sender.sendMessage(ChatColor.YELLOW + "Use the " + ChatColor.GOLD + "Gold Hoe" + ChatColor.YELLOW
                + " to select two corners.");
        sender.sendMessage("");
    }
}
