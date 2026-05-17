package net.frozenorb.foxtrot.gameplay.events.koth.commands.koth;

import cc.fyre.proton.command.Command;
import net.frozenorb.foxtrot.gameplay.events.koth.menu.KOTHEditorMenu;
import org.bukkit.entity.Player;

public class KOTHEditorCommand {

    @Command(names = {"koth editor"}, permission = "foxtrot.koth.admin")
    public static void kothEditor(Player player) {
        new KOTHEditorMenu().openMenu(player);
    }
}
