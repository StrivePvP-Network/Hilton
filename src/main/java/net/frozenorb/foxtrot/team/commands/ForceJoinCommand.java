package net.frozenorb.foxtrot.team.commands;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.common.location.ApolloBlockLocation;
import com.lunarclient.apollo.module.waypoint.Waypoint;
import com.lunarclient.apollo.module.waypoint.WaypointModule;
import com.lunarclient.apollo.player.ApolloPlayer;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.listener.LunarClientListener;
import net.frozenorb.foxtrot.team.Team;
import cc.fyre.proton.command.Command;
import cc.fyre.proton.command.param.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.Optional;

public class ForceJoinCommand {

    @Command(names={ "ForceJoin" }, permission="foxtrot.forcejoin")
    public static void forceJoin(Player sender, @Parameter(name="team") Team team, @Parameter(name="player", defaultValue="self") Player player) {
        if (Foxtrot.getInstance().getTeamHandler().getTeam(player) != null) {
            if (player == sender) {
                sender.sendMessage(ChatColor.RED + "Leave your current team before attempting to forcejoin.");
            } else {
                sender.sendMessage(ChatColor.RED + "That player needs to leave their current team first!");
            }

            return;
        }

        team.addMember(player.getUniqueId());
        Foxtrot.getInstance().getTeamHandler().setTeam(player.getUniqueId(), team);
        player.sendMessage(ChatColor.YELLOW + "You are now a member of " + ChatColor.LIGHT_PURPLE + team.getName() + ChatColor.YELLOW + "!");

        WaypointModule waypointModule = Apollo.getModuleManager().getModule(WaypointModule.class);
        Optional<ApolloPlayer> apolloPlayer = Apollo.getPlayerManager().getPlayer(player.getUniqueId());

        if (apolloPlayer.isPresent()) {
            if (team.getHQ() != null) {
                Waypoint hqWp = buildWaypoint("HQ", team.getHQ(), Color.BLUE);
                team.setHomeWaypoint(hqWp);
                waypointModule.displayWaypoint(apolloPlayer.get(), hqWp);
            }

            if (team.getRallyPoint() != null) {
                Waypoint rallyWp = buildWaypoint("Rally", team.getRallyPoint(), Color.ORANGE);
                team.setRallyWaypoint(rallyWp);
                waypointModule.displayWaypoint(apolloPlayer.get(), rallyWp);
            }

            if (team.getFocusedTeam() != null && team.getFocusedTeam().getHQ() != null) {
                Location location = team.getFocusedTeam().getHQ();
                Waypoint focusWp = buildWaypoint(team.getFocusedTeam().getName() + "'s HQ", location, Color.RED);
                team.setFocusWaypoint(focusWp);
                waypointModule.displayWaypoint(apolloPlayer.get(), focusWp);
            }
        }

        LunarClientListener.updateNametag(player);

        if (player != sender) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.YELLOW + " added to " + ChatColor.LIGHT_PURPLE + team.getName() + ChatColor.YELLOW + "!");
        }
    }

    private static Waypoint buildWaypoint(String name, Location loc, Color color) {
        return Waypoint.builder()
                .name(name)
                .location(ApolloBlockLocation.builder()
                        .world(loc.getWorld().getName())
                        .x(loc.getBlockX())
                        .y(loc.getBlockY())
                        .z(loc.getBlockZ())
                        .build())
                .color(color)
                .preventRemoval(false)
                .hidden(false)
                .build();
    }
}