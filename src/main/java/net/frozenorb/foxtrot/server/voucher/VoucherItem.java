package net.frozenorb.foxtrot.server.voucher;

import cc.fyre.neutron.Neutron;
import cc.fyre.neutron.profile.Profile;
import cc.fyre.neutron.profile.attributes.grant.Grant;
import cc.fyre.proton.util.ItemBuilder;
import net.buycraft.plugin.data.Coupon;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.util.CC;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.ChatColor;
import java.text.NumberFormat;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class VoucherItem {

    public static ItemStack create(VoucherType type, String value, String duration, String displayName) {
        ItemBuilder builder = ItemBuilder.of(Material.PAPER);
        builder.name(CC.translate(displayName));

        ArrayList<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7&m--------------------------"));
        lore.add(CC.translate("&eThis is a physical voucher."));
        lore.add(CC.translate("&eRight-click to redeem your reward!"));
        lore.add("");
        lore.add(CC.translate("&6&lReward Details:"));

        switch (type) {
            case RANK:
                lore.add(CC.translate("&e&l┃ &fRank: &c" + value));
                lore.add(CC.translate(
                        "&e&l┃ &fDuration: &c" + (duration.equalsIgnoreCase("perm") ? "Lifetime" : duration)));
                break;
            case AMOUNT:
                lore.add(CC.translate("&e&l┃ &fValue: &2$&a" + value));
                break;
            case BUNDLE:
                lore.add(CC.translate("&e&l┃ &fBundle: &b" + value));
                break;
            case COMMAND:
                lore.add(CC.translate("&e&l┃ &fReward: &7(Special Reward)"));
                break;
            case MONEY:
                lore.add(CC.translate("&e&l┃ &fAmount: &2$&a" + value));
                break;
            case LIVES:
                lore.add(CC.translate("&e&l┃ &fLives: &b" + value));
                break;
        }

        lore.add("");
        lore.add(CC.translate("&7&oThis item is unique and can be traded."));
        lore.add(CC.translate("&7&m--------------------------"));

        lore.add(CC.translate("&0&kHIDDEN:" + type.name() + ":" + value + ":" + duration));

        builder.setLore(lore);
        return builder.build();
    }

    public static void redeem(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasLore())
            return;

        String hiddenLine = null;
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("HIDDEN:")) {
                hiddenLine = ChatColor.stripColor(line);
                break;
            }
        }

        if (hiddenLine == null)
            return;

        String[] parts = hiddenLine.replace("HIDDEN:", "").split(":");
        if (parts.length < 2)
            return;

        VoucherType type = VoucherType.valueOf(parts[0]);
        String value = parts[1];
        String duration = parts.length > 2 ? parts[2] : "";

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().removeItem(item);
        }

        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
        player.sendMessage(CC.translate(
                "&a&lVoucher &7» &fYou have successfully redeemed a &e" + type.name().toLowerCase() + " &fvoucher!"));

        switch (type) {
            case RANK:
                Profile profile = Neutron.getInstance().getProfileHandler().fromUuid(player.getUniqueId());
                if (profile != null) {
                    cc.fyre.neutron.rank.Rank rank = Neutron.getInstance().getRankHandler().fromName(value);
                    if (rank != null) {
                        long durationLong = duration.equalsIgnoreCase("perm") ? Long.MAX_VALUE
                                : cc.fyre.proton.util.TimeUtils.parseTime(duration) * 1000L;

                        Grant grant = new Grant(
                                rank,
                                player.getUniqueId(),
                                Long.valueOf(durationLong),
                                "Auto-generated voucher");

                        profile.getGrants().add(grant);
                        profile.recalculateGrants();
                        profile.save();
                        player.sendMessage(CC.translate(
                                "&aThe rank &e" + rank.getColor() + rank.getName()
                                        + " &ahas been applied to your account."));
                    }
                }
                break;

            case AMOUNT:
                int amount = Integer.parseInt(value);
                generateBuycraftCoupon(player, amount);
                break;

            case BUNDLE:
                Bundles bundle = Arrays.stream(Bundles.values())
                        .filter(it -> it.getId().equalsIgnoreCase(value))
                        .findFirst().orElse(null);
                if (bundle != null) {
                    for (String command : bundle.getCommands()) {
                        Foxtrot.getInstance().getServer().dispatchCommand(
                                Foxtrot.getInstance().getServer().getConsoleSender(),
                                command.replace("{player}", player.getName()));
                    }
                    player.sendMessage(CC.translate("&aThe bundle rewards have been added to your account."));
                }
                break;

            case COMMAND:
                String cmd = value.replace("_", " ");
                Foxtrot.getInstance().getServer().dispatchCommand(
                        Foxtrot.getInstance().getServer().getConsoleSender(),
                        cmd.replace("{player}", player.getName()));
                break;

            case MONEY:
                double moneyAmount = Double.parseDouble(value);
                Foxtrot.getInstance().getEconomyHandler().deposit(player.getUniqueId(), moneyAmount);
                String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(moneyAmount);

                Foxtrot.getInstance().getServer().broadcastMessage("");
                Foxtrot.getInstance().getServer().broadcastMessage(CC.translate("&4&lMoney Voucher"));
                Foxtrot.getInstance().getServer().broadcastMessage(
                        ChatColor.WHITE + player.getName() + ChatColor.YELLOW + " has redeemed a " + ChatColor.GREEN
                                + "$" + formattedAmount + ChatColor.YELLOW + " Money Voucher!");
                Foxtrot.getInstance().getServer().broadcastMessage(
                        ChatColor.GRAY + "Purchase your own at " + ChatColor.WHITE + "store.strivepvp.top");
                Foxtrot.getInstance().getServer().broadcastMessage("");

                player.sendMessage(ChatColor.GREEN + "$" + formattedAmount + " has been added to your balance!");
                VoucherCommand.spawnFireworks(player.getLocation(), 5, 2, org.bukkit.Color.GREEN,
                        org.bukkit.FireworkEffect.Type.BALL_LARGE);
                break;

            case LIVES:
                int livesAmount = Integer.parseInt(value);
                int currentLives = Foxtrot.getInstance().getFriendLivesMap().getLives(player.getUniqueId());
                Foxtrot.getInstance().getFriendLivesMap().setLives(player.getUniqueId(), currentLives + livesAmount);

                Foxtrot.getInstance().getServer().broadcastMessage("");
                Foxtrot.getInstance().getServer().broadcastMessage(CC.translate("&4&lLife Voucher"));
                Foxtrot.getInstance().getServer().broadcastMessage(
                        ChatColor.WHITE + player.getName() + ChatColor.YELLOW + " has redeemed a " + ChatColor.GREEN
                                + livesAmount + " lives " + ChatColor.YELLOW + "Life Voucher!");
                Foxtrot.getInstance().getServer().broadcastMessage(
                        ChatColor.GRAY + "Purchase your own at " + ChatColor.WHITE + "store.strivepvp.top");
                Foxtrot.getInstance().getServer().broadcastMessage("");

                player.sendMessage(ChatColor.GREEN + "" + livesAmount + " lives have been added to your account!");
                VoucherCommand.spawnFireworks(player.getLocation(), 5, 2, org.bukkit.Color.GREEN,
                        org.bukkit.FireworkEffect.Type.BALL_LARGE);
                break;
        }
    }

    private static void generateBuycraftCoupon(Player player, int amount) {
        final Coupon.Discount discount = new Coupon.Discount("value", new BigDecimal("0.0"), new BigDecimal(amount));
        final Calendar expiration = Calendar.getInstance();
        expiration.set(Calendar.YEAR, 2030);

        final Coupon coupon = Coupon.builder()
                .note("Physical Voucher generated by " + player.getName())
                .basketType("single")
                .code(RandomStringUtils.random(10, true, true))
                .userLimit(1)
                .redeemUnlimited(false)
                .startDate(new Date(System.currentTimeMillis()))
                .minimum(new BigDecimal("0.0"))
                .expire(new Coupon.Expire("limit", 1, expiration.getTime()))
                .discountMethod(2)
                .effective(new Coupon.Effective("packages",
                        Foxtrot.getInstance().getVoucherHandler().getEffectiveHandler().getEffectivePackages(),
                        new ArrayList<>()))
                .startDate(new Date(System.currentTimeMillis()))
                .discount(discount).build();

        Foxtrot.getInstance().getServer().getScheduler().runTaskAsynchronously(Foxtrot.getInstance(), () -> {
            try {
                Foxtrot.getInstance().getBuycraftPlugin().getApiClient().createCoupon(coupon).execute();
                player.sendMessage("");
                player.sendMessage(CC.translate("&4&lVouchers"));
                player.sendMessage(CC.translate("&7Successfully generated your Buycraft code: &c" + coupon.getCode()));
                player.sendMessage(CC.translate("&7Use it at &fstore.strivepvp.top"));
                player.sendMessage("");
            } catch (IOException e) {
                player.sendMessage(CC.translate("&cFailed to generate Buycraft coupon. Please contact an admin."));
                e.printStackTrace();
            }
        });
    }

    public enum VoucherType {
        RANK, AMOUNT, BUNDLE, COMMAND, MONEY, LIVES
    }
}
