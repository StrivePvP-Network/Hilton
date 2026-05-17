package net.frozenorb.foxtrot.gameplay.events.koth.commands.koth;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.frozenorb.foxtrot.gameplay.events.Event;
import net.frozenorb.foxtrot.gameplay.events.EventType;
import net.frozenorb.foxtrot.gameplay.events.koth.KOTH;
import cc.fyre.proton.command.Command;
import cc.fyre.proton.command.param.Parameter;
import net.frozenorb.foxtrot.Foxtrot;

public class KOTHSetRewardCommand {

    @Command(names={ "KOTH SetPointsReward", "KOTH SetPoints" }, permission="foxtrot.koth.admin")
    public static void kothSetReward(Player sender, @Parameter(name="koth") Event koth, @Parameter(name="points") int points) {
        if (koth.getType() != EventType.KOTH) {
            sender.sendMessage(ChatColor.RED + "Unable to modify reward for a non-KOTH event.");
        } else {
            ((KOTH) koth).setPointsReward(points);
            Foxtrot.getInstance().getEventHandler().saveEvents();
            sender.sendMessage(ChatColor.GRAY + "Set points reward for the " + koth.getName() + " KOTH to " + points + ".");
        }
    }

}
