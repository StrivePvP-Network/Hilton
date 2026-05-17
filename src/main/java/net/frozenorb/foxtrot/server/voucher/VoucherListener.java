package net.frozenorb.foxtrot.server.voucher;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class VoucherListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            for (String line : item.getItemMeta().getLore()) {
                if (line.contains("HIDDEN:")) {
                    event.setCancelled(true);
                    VoucherItem.redeem(event.getPlayer(), item);
                    break;
                }
            }
        }
    }
}
