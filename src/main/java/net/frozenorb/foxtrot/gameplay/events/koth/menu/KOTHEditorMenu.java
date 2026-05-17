package net.frozenorb.foxtrot.gameplay.events.koth.menu;

import cc.fyre.proton.menu.Button;
import cc.fyre.proton.menu.Menu;
import cc.fyre.proton.util.ItemBuilder;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.gameplay.events.Event;
import net.frozenorb.foxtrot.gameplay.events.EventType;
import net.frozenorb.foxtrot.gameplay.events.koth.KOTH;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KOTHEditorMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return ChatColor.GOLD + ChatColor.BOLD.toString() + "KOTH Editor";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        int slot = 0;
        for (Event event : Foxtrot.getInstance().getEventHandler().getEvents()) {
            if (event instanceof KOTH) {
                buttons.put(slot++, new KOTHButton((KOTH) event));
            }
        }

        return buttons;
    }

    @Override
    public int size(Player player) {
        return 54;
    }

    private static class KOTHButton extends Button {
        private final KOTH koth;

        public KOTHButton(KOTH koth) {
            this.koth = koth;
        }

        @Override
        public String getName(Player player) {
            String color = koth.getKothColor() == null ? "&9" : koth.getKothColor();
            return ChatColor.translateAlternateColorCodes('&', color + koth.getName());
        }

        @Override
        public List<String> getDescription(Player player) {
            List<String> description = new ArrayList<>();
            description.add(ChatColor.GRAY + "World: " + ChatColor.WHITE + koth.getWorld());
            description.add(ChatColor.GRAY + "Cap Time: " + ChatColor.WHITE + (koth.getCapTime() / 60) + "m");
            description.add(ChatColor.GRAY + "Points: " + ChatColor.WHITE + koth.getPointsReward());
            description.add("");
            description.add(ChatColor.YELLOW + "Click to manage settings.");
            description.add(ChatColor.YELLOW + "Right-Click to teleport.");
            return description;
        }

        @Override
        public Material getMaterial(Player player) {
            return Material.BEACON;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType) {
            if (clickType.isRightClick()) {
                player.teleport(koth.getCapLocation().toLocation(Foxtrot.getInstance().getServer().getWorld(koth.getWorld())));
                player.sendMessage(ChatColor.GREEN + "Teleported to " + koth.getName() + ".");
                return;
            }

            new KOTHAjustesMenu(koth).openMenu(player);
        }
    }
}
