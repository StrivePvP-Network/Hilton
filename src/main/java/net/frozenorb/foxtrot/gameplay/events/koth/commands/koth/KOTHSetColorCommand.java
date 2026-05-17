package net.frozenorb.foxtrot.gameplay.events.koth.commands.koth;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.frozenorb.foxtrot.gameplay.events.Event;
import net.frozenorb.foxtrot.gameplay.events.EventType;
import net.frozenorb.foxtrot.gameplay.events.koth.KOTH;
import cc.fyre.proton.command.Command;
import cc.fyre.proton.command.param.Parameter;
import net.frozenorb.foxtrot.Foxtrot;

public class KOTHSetColorCommand {

    @Command(names={ "KOTH SetColor", "KOTH Color" }, permission="foxtrot.koth.admin")
    public static void kothSetColor(Player sender, @Parameter(name="koth") Event koth, @Parameter(name="color") String color) {
        if (koth.getType() != EventType.KOTH) {
            sender.sendMessage(ChatColor.RED + "Unable to modify color for a non-KOTH event.");
        } else {
            ((KOTH) koth).setKothColor(color);
            Foxtrot.getInstance().getEventHandler().saveEvents();
            sender.sendMessage(ChatColor.GRAY + "Set color for the " + koth.getName() + " KOTH to " + ChatColor.translateAlternateColorCodes('&', color) + "this color" + ChatColor.GRAY + ".");
        }
    }

}
