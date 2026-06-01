package net.frozenorb.foxtrot.scoreboard;

import cc.fyre.proton.scoreboard.construct.TitleGetter;
import net.frozenorb.foxtrot.Foxtrot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FoxtrotTitleGetter extends TitleGetter {

    public static boolean domain = false;

    public FoxtrotTitleGetter() {
        Bukkit.getServer().getScheduler().runTaskTimer(Foxtrot.getInstance(), () -> {
            domain = !domain;
        }, 100L, 100L);
    }

    @Override
    public String getTitle(Player player) {
        return ChatColor.translateAlternateColorCodes('&', "&4&lStrivePvP &7┃ &bSouls");
    }
}
