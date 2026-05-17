package net.frozenorb.foxtrot.gameplay.events.koth.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.gameplay.events.EventType;
import net.frozenorb.foxtrot.gameplay.events.koth.KOTH;
import net.frozenorb.foxtrot.gameplay.events.koth.events.EventControlTickEvent;
import cc.fyre.proton.util.TimeUtils;

public class KOTHListener implements Listener {

    @EventHandler
    public void onKOTHControlTick(EventControlTickEvent event) {
        if (event.getKOTH().getType() != EventType.KOTH) {
            return;
        }

        KOTH koth = event.getKOTH();
        if (koth.getRemainingCapTime() % 180 == 0 && koth.getRemainingCapTime() <= (koth.getCapTime() - 30)) {
            Foxtrot.getInstance().getServer().broadcastMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.YELLOW + koth.getName() + ChatColor.GOLD + " is trying to be controlled.");
            Foxtrot.getInstance().getServer().broadcastMessage(ChatColor.GOLD + " - Time left: " + ChatColor.BLUE + TimeUtils.formatIntoMMSS(koth.getRemainingCapTime()));
        }
    }

    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (event.getInventory().getTitle().startsWith("KOTH Loot: ")) {
            String kothName = event.getInventory().getTitle().replace("KOTH Loot: ", "");
            net.frozenorb.foxtrot.gameplay.events.Event eventObj = Foxtrot.getInstance().getEventHandler().getEvent(kothName);

            if (eventObj != null && eventObj instanceof KOTH) {
                KOTH koth = (KOTH) eventObj;
                koth.getLoot().clear();

                for (org.bukkit.inventory.ItemStack item : event.getInventory().getContents()) {
                    if (item != null && item.getType() != org.bukkit.Material.AIR) {
                        koth.getLoot().add(item);
                    }
                }

                Foxtrot.getInstance().getEventHandler().saveEvents();
                ((org.bukkit.entity.Player) event.getView().getPlayer()).sendMessage(ChatColor.GREEN + "Saved loot for the " + koth.getName() + " KOTH.");
            }
        }
    }

}