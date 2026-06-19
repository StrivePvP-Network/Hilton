package net.frozenorb.foxtrot.staff;

import cc.fyre.proton.Proton;
import cc.fyre.proton.command.Command;
import cc.fyre.proton.command.param.Parameter;
import cc.fyre.proton.util.ItemBuilder;
import lombok.Getter;
import net.frozenorb.foxtrot.Foxtrot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class StaffModeHandler {

    public enum VanishState {
        VISIBLE,
        STAFF,
        INVISIBLE
    }

    @Getter
    private final Set<UUID> modMode = new HashSet<>();
    private final Map<UUID, VanishState> vanishState = new HashMap<>();

    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();

    public StaffModeHandler() {
        Foxtrot.getInstance().getServer().getPluginManager().registerEvents(new StaffModeListener(this),
                Foxtrot.getInstance());
        Proton.getInstance().getCommandHandler().registerClass(this.getClass());

    }

    public boolean isInModMode(UUID uuid) {
        return modMode.contains(uuid);
    }

    public boolean isInVanish(UUID uuid) {
        return vanishState.getOrDefault(uuid, VanishState.VISIBLE) != VanishState.VISIBLE;
    }

    public VanishState getVanishState(UUID uuid) {
        return vanishState.getOrDefault(uuid, VanishState.VISIBLE);
    }

    @Command(names = { "mod", "h", "modmode", "staff", "staffmode" }, permission = "foxtrot.staff")
    public static void mod(Player player) {
        StaffModeHandler handler = Foxtrot.getInstance().getStaffModeHandler();

        if (handler.isInModMode(player.getUniqueId())) {
            handler.disableModMode(player);
        } else {
            handler.enableModMode(player);
        }
    }

    @Command(names = { "v", "vanish", "vis" }, permission = "foxtrot.staff")
    public static void vanish(Player player) {
        StaffModeHandler handler = Foxtrot.getInstance().getStaffModeHandler();

        VanishState currentState = handler.getVanishState(player.getUniqueId());

        if (currentState == VanishState.VISIBLE) {
            handler.setVanish(player, VanishState.STAFF);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Vanish: &eEnabled"));
        } else if (currentState == VanishState.STAFF) {
            handler.setVanish(player, VanishState.INVISIBLE);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Vanish: &7Invisible for all"));
        } else {
            handler.setVanish(player, VanishState.VISIBLE);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Vanish: &cDisabled"));
        }
    }

    public void enableModMode(Player player) {
        modMode.add(player.getUniqueId());

        savedInventories.put(player.getUniqueId(), player.getInventory().getContents());
        savedArmor.put(player.getUniqueId(), player.getInventory().getArmorContents());

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setGameMode(GameMode.CREATIVE);

        setVanish(player, VanishState.STAFF);
        giveModItems(player);

        player.sendMessage(ChatColor.AQUA + "Mod Mode: " + ChatColor.GREEN + "Enabled");
        net.frozenorb.foxtrot.listener.LunarClientListener.updateNametag(player);
    }

    public void disableModMode(Player player) {
        modMode.remove(player.getUniqueId());

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setGameMode(GameMode.SURVIVAL);

        if (savedInventories.containsKey(player.getUniqueId())) {
            player.getInventory().setContents(savedInventories.remove(player.getUniqueId()));
        }

        if (savedArmor.containsKey(player.getUniqueId())) {
            player.getInventory().setArmorContents(savedArmor.remove(player.getUniqueId()));
        }

        setVanish(player, VanishState.VISIBLE);

        player.sendMessage(ChatColor.AQUA + "Mod Mode: " + ChatColor.RED + "Disabled");
        net.frozenorb.foxtrot.listener.LunarClientListener.updateNametag(player);
    }

    public void setVanish(Player player, VanishState state) {
        vanishState.put(player.getUniqueId(), state);

        if (state != VanishState.VISIBLE) {
            player.setMetadata("invisible", new FixedMetadataValue(Foxtrot.getInstance(), true));
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (state == VanishState.INVISIBLE) {
                    online.hidePlayer(player);
                } else if (state == VanishState.STAFF) {
                    if (!online.hasPermission("foxtrot.staff") && !online.getUniqueId().equals(player.getUniqueId())) {
                        online.hidePlayer(player);
                    } else {
                        online.showPlayer(player);
                    }
                }
            }
        } else {
            player.removeMetadata("invisible", Foxtrot.getInstance());
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(player);
            }
        }

        if (isInModMode(player.getUniqueId())) {
            short data = state == VanishState.VISIBLE ? (short) 8
                    : (state == VanishState.STAFF ? (short) 10 : (short) 5);
            player.getInventory().setItem(8, ItemBuilder.of(Material.INK_SACK).data(data)
                    .name(ChatColor.AQUA + "Vanish Toggle").build());
            player.updateInventory();
        }

        net.frozenorb.foxtrot.listener.LunarClientListener.updateNametag(player);
    }

    public void giveModItems(Player player) {
        player.getInventory().clear();

        player.getInventory().setItem(0, ItemBuilder.of(Material.COMPASS).name(ChatColor.AQUA + "Compass").build());
        player.getInventory().setItem(1, ItemBuilder.of(Material.BOOK).name(ChatColor.AQUA + "Invsee").build());
        player.getInventory().setItem(3,
                ItemBuilder.of(Material.LEASH).name(ChatColor.AQUA + "Teleport to PvP").build());
        player.getInventory().setItem(4,
                ItemBuilder.of(Material.DIAMOND).name(ChatColor.AQUA + "Random Teleport").build());
        player.getInventory().setItem(5, ItemBuilder.of(Material.ICE).name(ChatColor.AQUA + "Freeze").build());
        player.getInventory().setItem(8,
                ItemBuilder.of(Material.INK_SACK).data((short) 10).name(ChatColor.AQUA + "Vanish Toggle").build());

        player.updateInventory();
    }

}
