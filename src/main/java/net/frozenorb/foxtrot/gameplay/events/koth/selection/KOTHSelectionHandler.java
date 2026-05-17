package net.frozenorb.foxtrot.gameplay.events.koth.selection;

import lombok.Getter;
import lombok.Setter;
import net.frozenorb.foxtrot.gameplay.events.koth.KOTH;
import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KOTHSelectionHandler {
    @Getter private static final Map<UUID, Selection> selections = new HashMap<>();

    public static class Selection {
        @Getter @Setter private KOTH koth;
        @Getter @Setter private Location point1;
        @Getter @Setter private Location point2;

        public Selection(KOTH koth) {
            this.koth = koth;
        }
    }
}
