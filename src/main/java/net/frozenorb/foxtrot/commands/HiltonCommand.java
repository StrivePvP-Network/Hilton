package net.frozenorb.foxtrot.commands;

import cc.fyre.neutron.Neutron;
import cc.fyre.proton.command.Command;
import net.frozenorb.foxtrot.commands.menu.HiltonFeatureMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class HiltonCommand {

    @Command(names = { "hilton syncperms" }, permission = "hilton.syncperms", async = true)
    public static void syncperms(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Starting comprehensive permission discovery in Neutron...");

        try {
            Neutron.getInstance().getPermissionHandler().discoverPermissions();
            sender.sendMessage(ChatColor.GREEN + "Successfully synced all permissions to Neutron.");
            sender.sendMessage(ChatColor.GRAY + "They should now appear in the rank editor menu.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error occurred while syncing permissions.");
            e.printStackTrace();
        }
    }

    @Command(names = { "hilton features" }, permission = "hilton.features")
    public static void features(Player player) {
        new HiltonFeatureMenu().openMenu(player);
    }
}
