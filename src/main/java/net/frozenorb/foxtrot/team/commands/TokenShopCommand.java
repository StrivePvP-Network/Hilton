package net.frozenorb.foxtrot.team.commands;

import cc.fyre.proton.command.Command;
import net.frozenorb.foxtrot.team.menu.TokenShopMenu;
import org.bukkit.entity.Player;

public class TokenShopCommand {

    @Command(names = {"tokenshop", "tokenstore"}, permission = "")
    public static void execute(Player player) {
        new TokenShopMenu().openMenu(player);
    }
}
