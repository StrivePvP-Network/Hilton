package net.frozenorb.foxtrot.gameplay.events.koth.menu;

import cc.fyre.proton.menu.Button;
import cc.fyre.proton.menu.Menu;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.gameplay.events.koth.KOTH;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KOTHLootEditorMenu extends Menu {

    private final KOTH koth;

    public KOTHLootEditorMenu(KOTH koth) {
        this.koth = koth;
        setNoncancellingInventory(true);
        setUpdateAfterClick(false);
    }

    @Override
    public String getTitle(Player player) {
        return ChatColor.BLUE + "Edit Loot: " + koth.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (int i = 0; i < 45; i++) {
            if (i < koth.getLoot().size()) {
            }
        }

        buttons.put(49, new Button() {
            @Override
            public String getName(Player player) {
                return ChatColor.GREEN + ChatColor.BOLD.toString() + "SAVE";
            }

            @Override
            public List<String> getDescription(Player player) {
                List<String> desc = new ArrayList<>();
                desc.add(ChatColor.GRAY + "Click to save the current items");
                desc.add(ChatColor.GRAY + "as the KOTH loot.");
                return desc;
            }

            @Override
            public Material getMaterial(Player player) {
                return Material.NETHER_STAR;
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType) {
                save(player);
                player.sendMessage(ChatColor.GREEN + "Loot saved for " + koth.getName() + ".");
                new KOTHAjustesMenu(koth).openMenu(player);
            }
        });

        buttons.put(45, new Button() {
            @Override
            public String getName(Player player) {
                return ChatColor.RED + "Back";
            }

            @Override
            public List<String> getDescription(Player player) {
                return new ArrayList<>();
            }

            @Override
            public Material getMaterial(Player player) {
                return Material.ARROW;
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType) {
                new KOTHAjustesMenu(koth).openMenu(player);
            }
        });

        return buttons;
    }

    @Override
    public void onOpen(Player player) {
        for (int i = 0; i < Math.min(koth.getLoot().size(), 45); i++) {
            player.getOpenInventory().getTopInventory().setItem(i, koth.getLoot().get(i));
        }
    }

    @Override
    public void onClose(Player player) {
    }

    private void save(Player player) {
        List<ItemStack> newLoot = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            ItemStack item = player.getOpenInventory().getTopInventory().getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                newLoot.add(item);
            }
        }
        koth.getLoot().clear();
        koth.getLoot().addAll(newLoot);
        Foxtrot.getInstance().getEventHandler().saveEvents();
    }

    @Override
    public int size(Player player) {
        return 54;
    }
}
