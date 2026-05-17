package net.frozenorb.foxtrot.gameplay.events.koth.commands.koth;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.frozenorb.foxtrot.gameplay.events.Event;
import net.frozenorb.foxtrot.gameplay.events.EventType;
import net.frozenorb.foxtrot.gameplay.events.koth.KOTH;
import cc.fyre.proton.command.Command;
import cc.fyre.proton.command.param.Parameter;
import net.frozenorb.foxtrot.Foxtrot;

public class KOTHSetAntiCleanCommand {

    @Command(names = { "KOTH SetAntiClean", "KOTH AntiClean" }, permission = "foxtrot.koth.admin")
    public static void kothSetAntiClean(Player sender, @Parameter(name = "koth") Event koth,
            @Parameter(name = "radius") int radius) {
        if (koth.getType() != EventType.KOTH) {
            sender.sendMessage(ChatColor.RED + "Unable to modify anti-clean for a non-KOTH event.");
        } else {
            ((KOTH) koth).setAntiCleanRadius(radius);
            Foxtrot.getInstance().getEventHandler().saveEvents();
            sender.sendMessage(
                    ChatColor.GRAY + "Set anti-clean radius for the " + koth.getName() + " KOTH to " + radius + ".");
        }
    }

}
