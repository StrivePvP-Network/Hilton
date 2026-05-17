package net.frozenorb.foxtrot.gameplay.events.koth.commands.koth;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import cc.fyre.proton.command.Command;

public class KOTHHelpCommand {

    @Command(names = { "KOTH Help" }, permission = "foxtrot.koth")
    public static void kothHelp(Player sender) {
        sender.sendMessage(ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString()
                + "--------------------------------------------------");
        sender.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "KOTH Help");
        sender.sendMessage(ChatColor.RED + "/koth list - Lists KOTHs");
        sender.sendMessage(ChatColor.RED + "/koth activate <name> - Activates a KOTH");
        sender.sendMessage(ChatColor.RED + "/koth deactivate <name> - Deactivates a KOTH");
        sender.sendMessage(ChatColor.RED + "/koth loc <name> - Set a KOTH's cap location");
        sender.sendMessage(ChatColor.RED + "/koth time <name> <time> - Sets a KOTH's cap time");
        sender.sendMessage(ChatColor.RED + "/koth dist <name> <distance> - Sets a KOTH's cap distance");
        sender.sendMessage(ChatColor.RED + "/koth tp <name> - TPs to a KOTH's");
        sender.sendMessage(ChatColor.RED + "/koth create <name> - Creates a KOTH");
        sender.sendMessage(ChatColor.RED + "/koth delete <name> - Deletes a KOTH");
        sender.sendMessage(ChatColor.RED + "/koth setpoints <name> <points> - Sets KOTH points reward");
        sender.sendMessage(ChatColor.RED + "/koth setanticlean <name> <radius> - Sets KOTH anti-clean radius");
        sender.sendMessage(ChatColor.RED + "/koth setcolor <name> <color> - Sets KOTH scoreboard color");
        sender.sendMessage(ChatColor.RED + "/koth loot <name> - Edits KOTH loot");
        sender.sendMessage(ChatColor.RED + "/koth editor - Opens the KOTH GUI editor");
        sender.sendMessage(ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString()
                + "--------------------------------------------------");
    }

}