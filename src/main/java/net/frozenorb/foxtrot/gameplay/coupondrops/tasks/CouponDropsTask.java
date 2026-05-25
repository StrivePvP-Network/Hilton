package net.frozenorb.foxtrot.gameplay.coupondrops.tasks;

import cc.fyre.universe.UniverseAPI;
import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.common.location.ApolloBlockLocation;
import com.lunarclient.apollo.module.waypoint.Waypoint;
import com.lunarclient.apollo.module.waypoint.WaypointModule;
import com.lunarclient.apollo.player.ApolloPlayer;
import net.buycraft.plugin.data.Coupon;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.gameplay.coupondrops.listener.CouponDropsListener;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class CouponDropsTask extends BukkitRunnable {
    private Foxtrot instance;
    public static final List<String> ACTIVE_COUPONS;
    private Coupon coupon;

    public CouponDropsTask(Foxtrot instance, Coupon coupon) {
        this.instance = instance;
        this.coupon = coupon;
    }

    @Override
    public void run() {
        if (UniverseAPI.getServerName().contains("Dev")) {
            return;
        }

        int x = ThreadLocalRandom.current().nextInt(100,1000) - 500;
        int z = ThreadLocalRandom.current().nextInt(100,1000) - 500;

        final Location location = this.instance.getServer().getWorld("world").getHighestBlockAt(x, z).getLocation().add(new Vector(0, 1, 0)).clone();
        location.getChunk().load(true);

        Waypoint couponWaypoint = Waypoint.builder()
                .name(CouponDropsListener.COUPON_DROP_WAYPOINT_NAME)
                .location(ApolloBlockLocation.builder()
                        .world(location.getWorld().getName())
                        .x(location.getBlockX())
                        .y(location.getBlockY())
                        .z(location.getBlockZ())
                        .build())
                .color(Color.RED)
                .preventRemoval(true)
                .hidden(false)
                .build();

        WaypointModule waypointModule = Apollo.getModuleManager().getModule(WaypointModule.class);

        this.instance.getServer().getOnlinePlayers().forEach(player -> {
            player.sendMessage(" ");
            player.sendMessage(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Discount Drops");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7A book containing a Buycraft Coupon will drop in &f5 minutes&7!"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cLocation: &f" + x + ", " + location.getBlockY() + ", " + z));
            player.sendMessage(" ");

            Optional<ApolloPlayer> apolloPlayer = Apollo.getPlayerManager().getPlayer(player.getUniqueId());
            if (apolloPlayer.isPresent()) {
                waypointModule.displayWaypoint(apolloPlayer.get(), couponWaypoint);
            }
        });

        Foxtrot.getInstance().getCouponDropsHandler().setDropsIn(System.currentTimeMillis()+TimeUnit.MINUTES.toMillis(5));
        Foxtrot.getInstance().getCouponDropsHandler().setLocation(location.clone());

        final ItemStack itemStack = this.getItem(this.coupon);

        this.instance.getServer().getScheduler().runTaskLater(Foxtrot.getInstance(), () -> {
            final Item item = location.getWorld().dropItemNaturally(location, itemStack.clone());
            item.setMetadata("DONT_CLEAR", new FixedMetadataValue(Foxtrot.getInstance(), true));

            this.instance.getServer().getOnlinePlayers().forEach(player -> {
                player.sendMessage(" ");
                player.sendMessage(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Discount Drops");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7The book containing a Coupon has been dropped!"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cLocation: &f" + x + ", " + location.getBlockY() + ", " + z));
                player.sendMessage(" ");
            });

            ACTIVE_COUPONS.add(itemStack.getItemMeta().getDisplayName());
        }, 20L * TimeUnit.MINUTES.toSeconds(5L));
    }

    private ItemStack getItem(final Coupon coupon) {
        final ItemStack itemStack = new ItemStack(Material.BOOK_AND_QUILL, 1);
        final BookMeta book = (BookMeta) itemStack.getItemMeta();

        book.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&Discount Code &7&l┃ &f" + coupon.getDiscount().getPercentage() + "% off"));
        book.setLore(Collections.singletonList(ChatColor.YELLOW + "Within this book contains a buycraft coupon code!"));
        book.setTitle(ChatColor.RED + "§lCoupon Book");
        book.setPages("\n" + ChatColor.RED + "Apply this coupon code at checkout!\n" + ChatColor.GRAY + "https://store.strivepvp.top\n\n" + ChatColor.DARK_GREEN + "Code: " + ChatColor.GREEN + coupon.getCode());
        book.setAuthor("CavePvP");
        itemStack.setItemMeta(book);
        return itemStack;
    }

    static {
        ACTIVE_COUPONS = new ArrayList<>();
    }
}