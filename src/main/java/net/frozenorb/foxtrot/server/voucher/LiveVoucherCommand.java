package net.frozenorb.foxtrot.server.voucher;

import cc.fyre.proton.command.Command;
import cc.fyre.proton.command.param.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Hxmilton (JavaProject)
 * @Contact hxmiltonn (discord)
 */

public class LiveVoucherCommand {

    @Command(names = { "livevoucher create", "livevouchers create" }, permission = "foxtrot.command.livevoucher.create")
    public static void create(Player player, @Parameter(name = "lives") int lives) {
        if (lives <= 0) {
            player.sendMessage(ChatColor.RED + "Please specify a positive number of lives!");
            return;
        }

        ItemStack item = VoucherItem.create(
                VoucherItem.VoucherType.LIVES,
                String.valueOf(lives),
                "perm",
                "&4&l" + lives + " Lives &7┃ &bVoucher"
        );

        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "You have successfully created a life voucher with " + ChatColor.WHITE + lives + ChatColor.GREEN + " lives!");
    }
}
