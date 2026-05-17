package net.frozenorb.foxtrot.server.voucher.menu;

import cc.fyre.proton.menu.Button;
import cc.fyre.proton.menu.pagination.PaginatedMenu;
import cc.fyre.proton.util.ItemBuilder;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.server.voucher.Voucher;
import net.frozenorb.foxtrot.util.CC;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class VoucherEditorMenu extends PaginatedMenu {

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "Voucher Editor";
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();

        List<Voucher> vouchers = Foxtrot.getInstance().getVoucherHandler().getCache();
        for (int i = 0; i < vouchers.size(); i++) {
            buttons.put(i, new VoucherElement(vouchers.get(i), this));
        }

        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(4, new Button() {
            @Override
            public String getName(Player player) {
                return ChatColor.GREEN + ChatColor.BOLD.toString() + "Create New Voucher";
            }

            @Override
            public List<String> getDescription(Player player) {
                return Collections.singletonList(ChatColor.GRAY + "Click to create a new voucher.");
            }

            @Override
            public Material getMaterial(Player player) {
                return Material.NETHER_STAR;
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType) {
                new VoucherCreateTypeMenu().openMenu(player);
            }
        });

        return buttons;
    }
}
