package net.frozenorb.foxtrot.server.reclaim;

import lombok.Getter;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.server.reclaim.menu.ReclaimItemEditMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Hxmilton (JavaProject)
 * @Contact hxmiltonn (discord)
 */

public class ReclaimHandler implements Listener {

    private final Foxtrot instance;

    @Getter
    private final List<Reclaim> reclaims = new ArrayList<>();

    @Getter
    private File folder;

    public ReclaimHandler(Foxtrot instance) {
        this.instance = instance;
        this.instance.getServer().getPluginManager().registerEvents(this, this.instance);
        this.loadData();
    }

    public void loadData() {
        this.reclaims.clear();
        this.folder = new File(this.instance.getDataFolder(), "data/reclaims");

        if (!this.folder.exists()) {
            this.folder.mkdirs();
        }

        File[] files = this.folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".yml")) {
                    this.loadFile(file);
                }
            }
        }
    }

    private void loadFile(File file) {
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);

        String name = data.getString("name", file.getName().replace(".yml", ""));
        int priority = data.getInt("priority", 99);
        List<String> commands = data.getStringList("commands");

        List<ItemStack> items = new ArrayList<>();
        if (data.contains("items")) {
            List<?> rawList = data.getList("items");
            if (rawList != null) {
                for (Object obj : rawList) {
                    if (obj instanceof ItemStack) {
                        items.add((ItemStack) obj);
                    }
                }
            }
        }

        Reclaim reclaim = new Reclaim(name, priority, commands, items);
        this.reclaims.add(reclaim);
    }

    public void saveReclaim(Reclaim reclaim) {
        if (!this.folder.exists()) {
            this.folder.mkdirs();
        }

        File file = new File(this.folder, reclaim.getName() + ".yml");
        FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(file);
        reclaim.save(file, dataConfig);
    }

    public void deleteReclaim(Reclaim reclaim) {
        this.reclaims.remove(reclaim);
        File file = new File(this.folder, reclaim.getName() + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }

    public Optional<Reclaim> getReclaim(String name) {
        return this.reclaims.stream().filter(r -> r.getName().equalsIgnoreCase(name)).findFirst();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() != null && event.getInventory().getTitle().startsWith("Reclaim Items: ")) {
            Player player = (Player) event.getPlayer();
            String reclaimName = event.getInventory().getTitle().replace("Reclaim Items: ", "");

            Reclaim reclaim = this.getReclaim(reclaimName).orElse(null);
            if (reclaim == null) return;

            Inventory inv = event.getInventory();
            List<ItemStack> newItems = new ArrayList<>();

            for (ItemStack item : inv.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    newItems.add(item.clone());
                }
            }

            reclaim.setItems(newItems);
            this.saveReclaim(reclaim);

            player.sendMessage(ChatColor.GREEN + "Saved " + newItems.size() + " items to reclaim " + reclaim.getName() + ".");

            Bukkit.getScheduler().runTaskLater(instance, () -> {
                new ReclaimItemEditMenu(reclaim).openMenu(player);
            }, 1L);
        }
    }
}
