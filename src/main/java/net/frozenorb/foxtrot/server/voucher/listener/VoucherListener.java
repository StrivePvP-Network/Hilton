package net.frozenorb.foxtrot.server.voucher.listener;

import cc.fyre.neutron.Neutron;
import cc.fyre.neutron.profile.Profile;
import cc.fyre.neutron.profile.attributes.grant.Grant;
import cc.fyre.neutron.rank.Rank;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.server.voucher.VoucherTemplate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class VoucherListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT")) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.PAPER) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return;

        String templateId = null;
        for (String line : item.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("HIDDEN:")) {
                templateId = stripped.replace("HIDDEN:", "");
                break;
            }
        }

        if (templateId == null) return;

        VoucherTemplate template = Foxtrot.getInstance().getVoucherHandler().fromName(templateId).orElse(null);

        if (template == null) {
            player.sendMessage(ChatColor.RED + "This voucher template no longer exists.");
            return;
        }

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }
        player.updateInventory();

        String value = template.getValue();
        String duration = template.getDuration();
        String category = template.getCategory().toUpperCase();

        player.sendMessage(ChatColor.GREEN + "You have redeemed a " + ChatColor.translateAlternateColorCodes('&', template.getDisplayName()) + ChatColor.GREEN + "!");

        if (category.equals("RANK")) {
            Profile profile = Neutron.getInstance().getProfileHandler().fromUuid(player.getUniqueId());
            if (profile != null) {
                Rank rank = Neutron.getInstance().getRankHandler().fromName(value);
                if (rank != null) {
                    long durationLong = duration.equalsIgnoreCase("perm") ? Long.MAX_VALUE
                            : cc.fyre.proton.util.TimeUtils.parseTime(duration) * 1000L;

                    Grant grant = new Grant(rank, player.getUniqueId(), durationLong, "Voucher Redeemed");
                    profile.getGrants().add(grant);
                    profile.recalculateGrants();
                    profile.save();
                    player.sendMessage(ChatColor.GREEN + "The rank " + rank.getColor() + rank.getName() + ChatColor.GREEN + " has been applied to your account.");
                } else {
                    player.sendMessage(ChatColor.RED + "Error: The rank associated with this voucher no longer exists.");
                }
            }
        } else if (category.equals("COMMAND")) {
            String cmd = value.replace("_", " ").replace("{player}", player.getName());
            Foxtrot.getInstance().getServer().dispatchCommand(Foxtrot.getInstance().getServer().getConsoleSender(), cmd);
        } else if (category.equals("MONEY")) {
            try {
                double amount = Double.parseDouble(value);
                Foxtrot.getInstance().getEconomyHandler().deposit(player.getUniqueId(), amount);
                player.sendMessage(ChatColor.GREEN + "$" + amount + " has been added to your balance!");
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Error: Invalid money amount in voucher template.");
            }
        } else if (category.equals("LIVES")) {
            try {
                int livesAmount = Integer.parseInt(value);
                int currentLives = Foxtrot.getInstance().getFriendLivesMap().getLives(player.getUniqueId());
                Foxtrot.getInstance().getFriendLivesMap().setLives(player.getUniqueId(), currentLives + livesAmount);
                player.sendMessage(ChatColor.GREEN + "" + livesAmount + " lives have been added to your account!");
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Error: Invalid lives amount in voucher template.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Error: Unhandled voucher category '" + category + "'.");
        }
    }
}
