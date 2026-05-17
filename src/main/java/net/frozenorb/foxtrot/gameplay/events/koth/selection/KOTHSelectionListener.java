package net.frozenorb.foxtrot.gameplay.events.koth.selection;

import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.util.Cuboid;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class KOTHSelectionListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.GOLD_HOE)
            return;
        if (!KOTHSelectionHandler.getSelections().containsKey(event.getPlayer().getUniqueId()))
            return;

        KOTHSelectionHandler.Selection selection = KOTHSelectionHandler.getSelections()
                .get(event.getPlayer().getUniqueId());
        event.setCancelled(true);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selection.setPoint1(event.getClickedBlock().getLocation());
            event.getPlayer()
                    .sendMessage(ChatColor.GREEN + "Point 1 set for " + selection.getKoth().getName() + " capzone!");
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selection.setPoint2(event.getClickedBlock().getLocation());
            event.getPlayer()
                    .sendMessage(ChatColor.GREEN + "Point 2 set for " + selection.getKoth().getName() + " capzone!");
        }

        if (selection.getPoint1() != null && selection.getPoint2() != null) {
            selection.getKoth().setCaptureZone(new Cuboid(selection.getPoint1(), selection.getPoint2()));
            Foxtrot.getInstance().getEventHandler().saveEvents();

            event.getPlayer().sendMessage("");
            event.getPlayer().sendMessage(ChatColor.GOLD + "Capzone for " + ChatColor.WHITE
                    + selection.getKoth().getName() + ChatColor.GOLD + " has been successfully updated!");
            event.getPlayer().sendMessage(
                    ChatColor.GRAY + "Area: " + selection.getKoth().getCaptureZone().getVolume() + " blocks.");

            KOTHSelectionHandler.getSelections().remove(event.getPlayer().getUniqueId());
            event.getPlayer().getInventory().remove(Material.GOLD_HOE);
            event.getPlayer().updateInventory();
        }
    }
}
