package net.frozenorb.foxtrot.server.voucher;

import cc.fyre.neutron.Neutron;
import cc.fyre.neutron.profile.Profile;
import cc.fyre.neutron.profile.attributes.grant.Grant;
import cc.fyre.neutron.util.DurationWrapper;
import cc.fyre.proton.Proton;
import cc.fyre.proton.command.Command;
import cc.fyre.proton.command.param.Parameter;
import cc.fyre.proton.uuid.UUIDCache;
import cc.fyre.proton.util.UUIDUtils;
import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.data.Coupon;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.server.voucher.menu.VoucherEditorMenu;
import net.frozenorb.foxtrot.server.voucher.menu.VoucherMainMenu;
import net.frozenorb.foxtrot.server.voucher.menu.VoucherRedeemMenu;
import net.frozenorb.foxtrot.util.CC;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VoucherCommand {

    @Command(names = { "perkkeybroadcast" }, permission = "op")
    public static void broadcast(CommandSender sender, @Parameter(name = "target") Player target,
            @Parameter(name = "reward", wildcard = true) String reward) {
        final Server server = Foxtrot.getInstance().getServer();

        for (Player onlinePlayer : server.getOnlinePlayers()) {
            onlinePlayer.sendMessage("");
            onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lPerk Crate"));
            onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', target.getName() + " &chas won " + reward + "&c!"));
            onlinePlayer
                    .sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Purchase Perk Keys on our store at &fstore.strivepvp.top&7."));
            onlinePlayer.sendMessage("");
        }
    }

    @Command(names = { "voucher view", "vouchers view" }, permission = "command.voucher.view", hidden = true)
    public static void execute(CommandSender commandSender, @Parameter(name = "target") UUID target) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "You must be a player to do this command.");
            return;
        }

        final Player player = (Player) commandSender;

        new VoucherMainMenu(target).openMenu(player);

        player.sendMessage(ChatColor.GREEN + "Successfully opened GUI.");
    }

    @Command(names = { "voucher save" }, permission = "op")
    public static void execute(Player player) {
        Foxtrot.getInstance().getVoucherHandler().saveData();
        player.sendMessage(ChatColor.GREEN + "Saved all voucher data!");
    }

    @Command(names = { "voucher data" }, permission = "op")
    public static void data(Player player) {
        int amountGiven = 0;
        int amountRedeemed = 0;
        int ranks = 0;
        int ranksRedeemed = 0;

        for (Voucher voucher : Foxtrot.getInstance().getVoucherHandler().getCache()) {

            if (voucher.getAmount() != 0) {
                amountGiven += voucher.getAmount();
            }

            if (voucher.getAmount() != 0 && voucher.isUsed()) {
                amountRedeemed += voucher.getAmount();
            }

            if (voucher.isRank()) {
                ranks++;
            }

            if (voucher.isRank() && voucher.isUsed()) {
                ranksRedeemed++;
            }
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Amount Given: &2$&a" + amountGiven));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Amount Redeemed: &2$&a" + amountRedeemed));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Ranks Given: &f" + ranks));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Ranks Redeemed: &f" + ranksRedeemed));
    }

    @Command(names = { "massview" }, permission = "op", async = true)
    public static void massView(Player player, @Parameter(name = "duration") DurationWrapper duration) {

        if (!player.getName().equalsIgnoreCase("SimplyTrash")) {
            player.sendMessage(ChatColor.RED + "Command disabled atm");
            return;
        }

        player.sendMessage("Getting all vouchers from " + duration.getSource() + " to " + duration.getDuration());

        int amountGiven = 0;
        int amountRedeemed = 0;
        int ranks = 0;
        int ranksRedeemed = 0;
        int ranksRemoved = 0;

        final BuyCraftAPI api = Foxtrot.getInstance().getBuycraftPlugin().getApiClient();
        final List<Coupon> couponList = new ArrayList<>();
        try {
            player.sendMessage(ChatColor.GREEN + "Inserted list");
            couponList.addAll(api.getAllCoupons().execute().body().getData());
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Failed to insert list!");
            e.printStackTrace();
            return;
        }

        for (Coupon coupon : couponList) {
            final long startTime = coupon.getStartDate().getTime();

            if (startTime < System.currentTimeMillis() - duration.getDuration()) {
                continue;
            }

            try {
                for (Coupon datum : couponList) {
                    if (!datum.getCode().startsWith("ALF-IMAKE-KIT-CODE-14919") && !datum.getCode()
                            .startsWith("COPY-THIS-COUPON-AND-CHANGE-AMOUNT-AND-GENERATE-NEW-CODE")) {
                        api.deleteCoupon(datum.getId()).execute();
                        player.sendMessage(ChatColor.GREEN + "Identified & deleted " + datum.getCode() + "!");
                    } else {
                        player.sendMessage(datum.getCode() + " Doesnt begin ");
                    }

                }
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Failed! Check console");
                e.printStackTrace();
            }
        }

        for (Voucher voucher : Foxtrot.getInstance().getVoucherHandler().getCache()) {

            if (voucher.getAddedTime() < System.currentTimeMillis() - duration.getDuration()) {
                continue;
            }

            if (voucher.getAmount() != 0 && voucher.isUsed()) {
                final String code = voucher.getCode();
            }

            if (voucher.isRank() && voucher.isUsed()) {
                if (!voucher.getCode().contains("Given to ")) {
                    player.sendMessage(ChatColor.RED + "Doesnt exist!");
                    return;
                }

                final String playerName = voucher.getCode().replace("Given to ", "");
                final String usedBy = UUIDUtils.name(voucher.getTarget());

                if (UUIDUtils.uuid(playerName) == null) {
                    player.sendMessage(ChatColor.RED + "Invalid UUID");
                    continue;
                }

                final Profile profile = Neutron.getInstance().getProfileHandler().fromName(playerName, true, true);

                player.sendMessage("Trying " + playerName);

                if (profile == null) {
                    player.sendMessage(ChatColor.RED + "PROFILE NOT FOUND!");
                    continue;
                }

                for (Grant grant : profile.getGrants()) {

                    if (grant.getExecutedAt() < System.currentTimeMillis() - duration.getDuration()) {
                        continue;
                    }

                    if (grant.isPardoned()) {
                        continue;
                    }

                    if (grant.getExecutedReason().contains("Auto-generated voucher")
                            && grant.getExecutedReason().contains(usedBy)
                            && grant.getRank().getName().contains(voucher.getRankName())) {
                        ranksRemoved++;
                        player.sendMessage(ChatColor.GREEN + "Deleted a rank, found " + grant.getRank().getName()
                                + " on " + ChatColor.WHITE + playerName + " removed " + ranksRemoved);

                        grant.setPardoner(player.getUniqueId());
                        grant.setPardonedAt(System.currentTimeMillis());
                        grant.setPardonedReason("DUPED DO NOT GIVE IT BACK");
                    }
                }

                profile.recalculateGrants();
                profile.save();
            }
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Amount Given: &2$&a" + amountGiven));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Amount Redeemed: &2$&a" + amountRedeemed));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Ranks Given: &f" + ranks));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Ranks Redeemed: &f" + ranksRedeemed));
    }

    @Command(names = { "massdelete" }, permission = "op", async = true)
    public static void execute(Player player, @Parameter(name = "lol") String begins) {

        if (!player.getName().equalsIgnoreCase("SimplyTrash")) {
            player.sendMessage(ChatColor.RED + "No permission");
            return;
        }

        final BuyCraftAPI api = Foxtrot.getInstance().getBuycraftPlugin().getApiClient();
        final List<Coupon> couponList = new ArrayList<>();
        try {
            player.sendMessage(ChatColor.GREEN + "Inserted list");
            couponList.addAll(api.getAllCoupons().execute().body().getData());
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Failed to insert list!");
            e.printStackTrace();
            return;
        }

        try {
            for (Coupon datum : couponList) {
                if (!datum.getCode().startsWith("ALF-IMAKE-KIT-CODE-14919")
                        && !datum.getCode().startsWith("COPY-THIS-COUPON-AND-CHANGE-AMOUNT-AND-GENERATE-NEW-CODE")) {
                    api.deleteCoupon(datum.getId()).execute();
                    player.sendMessage(ChatColor.GREEN + "Identified & deleted " + datum.getCode() + "!");
                } else {
                    player.sendMessage(datum.getCode() + " Doesnt begin ");
                }

            }
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Failed! Check console");
            e.printStackTrace();
        }

    }

    @Command(names = { "voucher clear", "vouchers clear" }, permission = "command.voucher.clear", hidden = true)
    public static void clear(CommandSender commandSender, @Parameter(name = "target") UUID target) {
        if (commandSender instanceof Player && !commandSender.getName().equalsIgnoreCase("SimplyTrash")) {
            commandSender.sendMessage(ChatColor.RED + "No permission!");
            return;
        }

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "You must be a player to do this command.");
            return;
        }
        final Player player = (Player) commandSender;

        Foxtrot.getInstance().getVoucherHandler().getCache().stream()
                .filter(it -> it.getTarget().toString().equalsIgnoreCase(target.toString()))
                .forEach(it -> Foxtrot.getInstance().getVoucherHandler().getCache().remove(it));

        player.sendMessage(ChatColor.GREEN + "Cleared all of their Vouchers");
    }

    @Command(names = { "voucher addrank", "vouchers addrank" }, permission = "command.voucher.rank", hidden = true)
    public static void addRank(CommandSender commandSender, @Parameter(name = "target") UUID target,
            @Parameter(name = "rankName") String rankName, @Parameter(name = "duration") String duration,
            @Parameter(name = "displayName") String displayName,
            @Parameter(name = "expiration", defaultValue = "none") String expiration) {
        if (commandSender instanceof Player && !commandSender.getName().equalsIgnoreCase("Hxmilton")
                && !commandSender.isOp()) {
            commandSender.sendMessage(ChatColor.RED + "No permission!");
            return;
        }

        final Voucher voucher = new Voucher(target, ChatColor.translateAlternateColorCodes('&', displayName), commandSender.getName());

        voucher.setRank(true);
        voucher.setRankName(rankName);
        voucher.setRankDuration(duration);

        if (!expiration.equalsIgnoreCase("none")) {
            try {
                long seconds = cc.fyre.proton.util.TimeUtils.parseTime(expiration);
                voucher.setExpirationTime(System.currentTimeMillis() + (seconds * 1000));
            } catch (Exception ignored) {
            }
        }

        commandSender.sendMessage(ChatColor.GREEN + "Added voucher: " + CC.translate(displayName) + ChatColor.GREEN
                + " to " + Proton.getInstance().getUuidCache().name(target) + "'s account.");

        final Player targetPlayer = Foxtrot.getInstance().getServer().getPlayer(target);

        if (targetPlayer == null) {
            return;
        }

        targetPlayer.sendMessage("");
        targetPlayer.sendMessage(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Voucher");
        targetPlayer.sendMessage(ChatColor.GRAY + "Congratulations! You have won a " + ChatColor.translateAlternateColorCodes('&', displayName)
                + ChatColor.GRAY + ".");
        targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType /voucher redeem to redeem your voucher!"));
        targetPlayer.sendMessage("");

        for (int i = 0; i < 5; i++) {
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.EXPLODE, 1.0F, 1.0F);
        }

        spawnFireworks(targetPlayer.getLocation(), 25, 3, Color.RED, FireworkEffect.Type.BALL_LARGE);
    }

    @Command(names = { "voucher addbundle", "vouchers addbundles" }, permission = "command.voucher.rank", hidden = true)
    public static void addBundle(CommandSender commandSender, @Parameter(name = "target") UUID target,
            @Parameter(name = "bundleID") String bundleID,
            @Parameter(name = "displayName") String displayName,
            @Parameter(name = "expiration", defaultValue = "none") String expiration) {
        if (commandSender instanceof Player && !commandSender.isOp()) {
            commandSender.sendMessage(ChatColor.RED + "No permission!");
            return;
        }

        final Voucher voucher = new Voucher(target, ChatColor.translateAlternateColorCodes('&', displayName), commandSender.getName());

        voucher.setBundle(true);
        voucher.setBundleID(bundleID);

        if (!expiration.equalsIgnoreCase("none")) {
            try {
                long seconds = cc.fyre.proton.util.TimeUtils.parseTime(expiration);
                voucher.setExpirationTime(System.currentTimeMillis() + (seconds * 1000));
            } catch (Exception ignored) {
            }
        }

        commandSender.sendMessage(ChatColor.GREEN + "Added voucher: " + CC.translate(displayName) + ChatColor.GREEN
                + " to " + Proton.getInstance().getUuidCache().name(target) + "'s account.");

        final Player targetPlayer = Foxtrot.getInstance().getServer().getPlayer(target);

        if (targetPlayer == null) {
            return;
        }

        targetPlayer.sendMessage("");
        targetPlayer.sendMessage(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Voucher");
        targetPlayer.sendMessage(ChatColor.GRAY + "Congratulations! You have won a " + ChatColor.translateAlternateColorCodes('&', displayName)
                + ChatColor.GRAY + ".");
        targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType /voucher redeem to redeem your voucher!"));
        targetPlayer.sendMessage("");

        for (int i = 0; i < 5; i++) {
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.EXPLODE, 1.0F, 1.0F);
        }

        spawnFireworks(targetPlayer.getLocation(), 25, 3, Color.RED, FireworkEffect.Type.BALL_LARGE);
    }

    @Command(names = { "voucher addamount", "vouchers addamount" }, permission = "command.voucher.rank", hidden = true)
    public static void addAmount(CommandSender commandSender, @Parameter(name = "target") UUID target,
            @Parameter(name = "amount") int amount,
            @Parameter(name = "expiration", defaultValue = "none") String expiration) {
        if (commandSender instanceof Player && !commandSender.isOp()) {
            commandSender.sendMessage(ChatColor.RED + "No permission!");
            return;
        }

        final Voucher voucher = new Voucher(target, ChatColor.translateAlternateColorCodes('&', "&2$&a" + amount + " Buycraft Voucher"),
                commandSender.getName());

        voucher.setAmount(amount);

        if (!expiration.equalsIgnoreCase("none")) {
            try {
                long seconds = cc.fyre.proton.util.TimeUtils.parseTime(expiration);
                voucher.setExpirationTime(System.currentTimeMillis() + (seconds * 1000));
            } catch (Exception ignored) {
            }
        }

        commandSender.sendMessage(
                ChatColor.GREEN + "Added voucher: " + ChatColor.translateAlternateColorCodes('&', "&2$&a" + amount + " Buycraft Voucher")
                        + ChatColor.GREEN + " to " + Proton.getInstance().getUuidCache().name(target) + "'s account.");

        final Player targetPlayer = Foxtrot.getInstance().getServer().getPlayer(target);

        if (targetPlayer == null) {
            return;
        }

        targetPlayer.sendMessage("");
        targetPlayer.sendMessage(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Voucher");
        targetPlayer.sendMessage(ChatColor.GRAY + "Congratulations! You have won a "
                + ChatColor.translateAlternateColorCodes('&', "&2$&a" + amount + " Buycraft Voucher") + ChatColor.GRAY + ".");
        targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType /voucher redeem to redeem your voucher!"));
        targetPlayer.sendMessage("");

        for (int i = 0; i < 5; i++) {
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.EXPLODE, 1.0F, 1.0F);
        }

        spawnFireworks(targetPlayer.getLocation(), 25, 3, Color.RED, FireworkEffect.Type.BALL_LARGE);
    }

    @Command(names = { "voucher redeem", "vouchers redeem" }, permission = "")
    public static void redeem(Player player) {
        new VoucherRedeemMenu().openMenu(player);
    }

    @Command(names = { "voucher add", "voucher create", "vouchers add",
            "vouchers create" }, permission = "command.voucher.create", hidden = true)
    public static void add(CommandSender commandSender, @Parameter(name = "target") UUID target,
            @Parameter(name = "voucher", wildcard = true) String text) {
        if (commandSender instanceof Player && !commandSender.getName().equalsIgnoreCase("SimplyTrash")) {
            commandSender.sendMessage(ChatColor.RED + "No permission!");
            return;
        }

        final Voucher voucher = new Voucher(target, CC.translate(text), commandSender.getName());

        commandSender.sendMessage(ChatColor.GREEN + "Added voucher: " + CC.translate(text) + ChatColor.GREEN + " to "
                + Proton.getInstance().getUuidCache().name(target) + "'s account.");

        final Player targetPlayer = Foxtrot.getInstance().getServer().getPlayer(target);

        if (targetPlayer == null || commandSender instanceof Player) {
            return;
        }

        targetPlayer.sendMessage("");
        targetPlayer.sendMessage(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Voucher");
        targetPlayer.sendMessage(
                ChatColor.GRAY + "Congratulations! You have won a " + CC.translate(text) + ChatColor.GRAY + ".");
        targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType /voucher redeem to redeem your voucher!"));
        targetPlayer.sendMessage("");

        for (int i = 0; i < 5; i++) {
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.EXPLODE, 1.0F, 1.0F);
        }

        spawnFireworks(targetPlayer.getLocation(), 25, 3, Color.RED, FireworkEffect.Type.BALL_LARGE);
    }

    @Command(names = { "voucher give" }, permission = "foxtrot.command.voucher.give")
    public static void giveVoucher(CommandSender sender, @Parameter(name = "target") Player target,
            @Parameter(name = "type") String typeStr, @Parameter(name = "value") String value,
            @Parameter(name = "duration", defaultValue = "perm") String duration,
            @Parameter(name = "displayName", wildcard = true) String displayName) {

        VoucherItem.VoucherType type;
        try {
            type = VoucherItem.VoucherType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid type! (RANK, AMOUNT, BUNDLE, COMMAND)");
            return;
        }

        ItemStack item = VoucherItem.create(type, value, duration, displayName);
        target.getInventory().addItem(item);

        sender.sendMessage(ChatColor.GREEN + "Gave " + ChatColor.WHITE + displayName + ChatColor.GREEN + " to "
                + target.getName());
    }

    @Command(names = { "voucher create" }, permission = "foxtrot.command.voucher.create")
    public static void createVoucher(Player player, @Parameter(name = "type") String typeStr,
            @Parameter(name = "value") String value,
            @Parameter(name = "duration", defaultValue = "perm") String duration,
            @Parameter(name = "displayName", wildcard = true) String displayName) {

        VoucherItem.VoucherType type;
        try {
            type = VoucherItem.VoucherType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid type! (RANK, AMOUNT, BUNDLE, COMMAND)");
            return;
        }

        ItemStack item = VoucherItem.create(type, value, duration, displayName);
        player.getInventory().addItem(item);

        player.sendMessage(ChatColor.GREEN + "You have created a " + ChatColor.WHITE + displayName);
    }

    @Command(names = { "voucher wipe", "vouchers wipe" }, permission = "foxtrot.command.voucher.wipe")
    public static void wipe(CommandSender sender) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return;
        }

        int vouchersRemoved = Foxtrot.getInstance().getVoucherHandler().getCache().size();
        Foxtrot.getInstance().getVoucherHandler().getCache().clear();
        Foxtrot.getInstance().getVoucherHandler().saveData();

        sender.sendMessage(ChatColor.GREEN + "Successfully wiped " + vouchersRemoved + " vouchers.");
        sender.sendMessage(ChatColor.YELLOW + "Starting rank wipe (this may take a moment)...");

        Foxtrot.getInstance().getServer().getScheduler().runTaskAsynchronously(Foxtrot.getInstance(), () -> {
            int ranksRemoved = 0;
            for (Player onlinePlayer : Foxtrot.getInstance().getServer().getOnlinePlayers()) {
                Profile profile = Neutron.getInstance().getProfileHandler().fromUuid(onlinePlayer.getUniqueId());
                if (profile == null)
                    continue;

                boolean changed = false;
                for (Grant grant : new ArrayList<>(profile.getGrants())) {
                    if (grant.getExecutedReason().contains("Auto-generated voucher") && !grant.isPardoned()) {
                        grant.setPardoner(UUIDCache.CONSOLE_UUID);
                        grant.setPardonedAt(System.currentTimeMillis());
                        grant.setPardonedReason("Voucher Wipe");
                        ranksRemoved++;
                        changed = true;
                    }
                }

                if (changed) {
                    profile.recalculateGrants();
                    profile.save();
                    onlinePlayer.sendMessage(ChatColor.RED + "Your voucher-granted ranks have been wiped.");
                }
            }
            sender.sendMessage(ChatColor.GREEN + "Rank wipe complete. Removed " + ranksRemoved
                    + " active ranks from online players.");
            sender.sendMessage(
                    ChatColor.GRAY + "Note: Offline players were not affected by the rank wipe to prevent lag.");
        });
    }

    @Command(names = { "voucher editor", "vouchers editor" }, permission = "foxtrot.command.voucher.editor")
    public static void editor(Player player) {
        new net.frozenorb.foxtrot.server.voucher.menu.VoucherEditorMenu().openMenu(player);
    }

    @Command(names = { "voucher giveaway", "vouchers giveaway" }, permission = "foxtrot.command.voucher.giveaway")
    public static void giveaway(CommandSender sender, @Parameter(name = "type") String type,
            @Parameter(name = "details", wildcard = true) String details) {
        String[] args = details.split(" ");
        int count = 0;

        String expiration = "none";
        // duration (3d, 7d, 30d)
        if (args.length > 1 && (args[args.length - 1].endsWith("d") || args[args.length - 1].endsWith("h")
                || args[args.length - 1].endsWith("m"))) {
            expiration = args[args.length - 1];
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 0, newArgs, 0, args.length - 1);
            args = newArgs;
        }

        for (Player target : Foxtrot.getInstance().getServer().getOnlinePlayers()) {
            if (type.equalsIgnoreCase("rank")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED
                            + "Usage: /voucher giveaway rank <rank> <duration> <displayName> [expiration]");
                    return;
                }
                String rank = args[0];
                String duration = args[1];
                StringBuilder displayName = new StringBuilder();
                for (int i = 2; i < args.length; i++)
                    displayName.append(args[i]).append(" ");

                addRank(sender, target.getUniqueId(), rank, duration, displayName.toString().trim(), expiration);
            } else if (type.equalsIgnoreCase("bundle")) {
                if (args.length < 2) {
                    sender.sendMessage(
                            ChatColor.RED + "Usage: /voucher giveaway bundle <bundleID> <displayName> [expiration]");
                    return;
                }
                String bundleID = args[0];
                StringBuilder displayName = new StringBuilder();
                for (int i = 1; i < args.length; i++)
                    displayName.append(args[i]).append(" ");

                addBundle(sender, target.getUniqueId(), bundleID, displayName.toString().trim(), expiration);
            } else if (type.equalsIgnoreCase("amount")) {
                if (args.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Usage: /voucher giveaway amount <amount> [expiration]");
                    return;
                }
                int amount = Integer.parseInt(args[0]);
                addAmount(sender, target.getUniqueId(), amount, expiration);
            }
            count++;
        }

        sender.sendMessage(ChatColor.GREEN + "Giveaway complete! Gave " + type + " voucher to " + count + " players.");
        Foxtrot.getInstance().getVoucherHandler().saveData();
    }

    public static void spawnFireworks(Location location, int amount, int power, Color color,
            FireworkEffect.Type fireworkEffect) {
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();

        fireworkMeta.setPower(power);
        fireworkMeta.addEffect(FireworkEffect.builder()
                .with(fireworkEffect)
                .with(FireworkEffect.Type.BURST)
                .withColor(color)
                .flicker(true)
                .build());
        firework.setFireworkMeta(fireworkMeta);

        // Spawn the additional amount
        for (int i = 0; i < amount; i++) {
            Firework entityFirework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
            entityFirework.setFireworkMeta(fireworkMeta);
        }
    }
}