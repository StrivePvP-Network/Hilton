package net.frozenorb.foxtrot.gameplay.ability.listener;

import cc.fyre.proton.util.TimeUtils;
import lombok.AllArgsConstructor;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.gameplay.ability.Ability;
import net.frozenorb.foxtrot.gameplay.ability.AbilityHandler;
import net.frozenorb.foxtrot.gameplay.ability.listener.events.AbilityUseEvent;
import net.frozenorb.foxtrot.team.Team;
import net.frozenorb.foxtrot.team.claims.LandBoard;
import net.frozenorb.foxtrot.team.event.TeamEnterClaimEvent;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class AbilityListener implements Listener {
    private Foxtrot instance;
    private AbilityHandler abilityHandler;

    @EventHandler(priority = EventPriority.LOW)
    private void onUse(AbilityUseEvent event) {
        final Player player = event.getPlayer();
        final Ability ability = event.getAbility();

        if (!abilityHandler.isAbilityEnabled(ability)) {
            player.sendMessage(ChatColor.RED + "The " + ability.getDisplayName() + ChatColor.RED + " is currently disabled.");
            event.setCancelled(true);
            return;
        }

        if (ability.hasCooldown(player)) {
            event.setCancelled(true);
            return;
        }

        final Location blockAt = event.getChosenLocation();

        if (blockAt.getWorld().getName().equalsIgnoreCase("sg")) {
            return;
        }

        if (!ability.isAllowedAtLocation(blockAt)) {
            String teamName;

            final Team ownerTeam = LandBoard.getInstance().getTeam(blockAt);

            if (ownerTeam != null) {
                teamName = ownerTeam.getName(player);
            } else if (!Foxtrot.getInstance().getServerHandler().isWarzone(blockAt)) {
                teamName = ChatColor.GRAY + "The Wilderness";
            } else {
                teamName = ChatColor.DARK_RED + "WarZone";
            }

            player.sendMessage(ChatColor.RED + "You cannot use a " + ability.getDisplayName() + ChatColor.RED + " in " + teamName + ChatColor.RED + ".");
            event.setCancelled(true);
            return;
        }

        if (event.isOneHit()) {
            return;
        }



//        abilityHandler.getGlobalCooldowns().put(player.getUniqueId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5));
    }
}
