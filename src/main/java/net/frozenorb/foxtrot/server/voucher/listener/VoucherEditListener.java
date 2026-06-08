package net.frozenorb.foxtrot.server.voucher.listener;

import net.frozenorb.foxtrot.server.voucher.VoucherTemplate;
import net.frozenorb.foxtrot.server.voucher.menu.VoucherEditMenu;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class VoucherEditListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        boolean cancel = false;

        if (VoucherEditMenu.getModifyingDisplay().containsKey(event.getPlayer().getUniqueId())) {
            VoucherTemplate template = VoucherEditMenu.getModifyingDisplay().remove(event.getPlayer().getUniqueId());

            if (!event.getMessage().equalsIgnoreCase("cancel")) {
                template.setDisplayName(ChatColor.translateAlternateColorCodes('&', event.getMessage()));
                template.save();
                event.getPlayer().sendMessage(ChatColor.GREEN + "Successfully updated the display name!");
            }
            
            new VoucherEditMenu(template).openMenu(event.getPlayer());
            cancel = true;
        }

        if (VoucherEditMenu.getModifyingCategory().containsKey(event.getPlayer().getUniqueId())) {
            VoucherTemplate template = VoucherEditMenu.getModifyingCategory().remove(event.getPlayer().getUniqueId());

            if (!event.getMessage().equalsIgnoreCase("cancel")) {
                template.setCategory(event.getMessage().toUpperCase());
                template.save();
                event.getPlayer().sendMessage(ChatColor.GREEN + "Successfully updated the category!");
            }
            
            new VoucherEditMenu(template).openMenu(event.getPlayer());
            cancel = true;
        }

        if (VoucherEditMenu.getModifyingValue().containsKey(event.getPlayer().getUniqueId())) {
            VoucherTemplate template = VoucherEditMenu.getModifyingValue().remove(event.getPlayer().getUniqueId());

            if (!event.getMessage().equalsIgnoreCase("cancel")) {
                template.setValue(event.getMessage());
                template.save();
                event.getPlayer().sendMessage(ChatColor.GREEN + "Successfully updated the value!");
            }
            
            new VoucherEditMenu(template).openMenu(event.getPlayer());
            cancel = true;
        }

        if (VoucherEditMenu.getModifyingDuration().containsKey(event.getPlayer().getUniqueId())) {
            VoucherTemplate template = VoucherEditMenu.getModifyingDuration().remove(event.getPlayer().getUniqueId());

            if (!event.getMessage().equalsIgnoreCase("cancel")) {
                template.setDuration(event.getMessage());
                template.save();
                event.getPlayer().sendMessage(ChatColor.GREEN + "Successfully updated the duration!");
            }
            
            new VoucherEditMenu(template).openMenu(event.getPlayer());
            cancel = true;
        }

        if (cancel) {
            event.getRecipients().clear();
            event.setCancelled(true);
        }
    }
}
