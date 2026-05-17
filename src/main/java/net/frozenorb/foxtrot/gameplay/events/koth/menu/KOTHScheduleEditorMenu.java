package net.frozenorb.foxtrot.gameplay.events.koth.menu;

import cc.fyre.proton.menu.Button;
import cc.fyre.proton.menu.Menu;
import cc.fyre.proton.util.ItemBuilder;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.gameplay.events.EventScheduledTime;
import net.frozenorb.foxtrot.gameplay.events.koth.KOTH;
import net.frozenorb.foxtrot.gameplay.events.koth.commands.koth.KOTHScheduleCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class KOTHScheduleEditorMenu extends Menu {

    private final KOTH koth;

    public KOTHScheduleEditorMenu(KOTH koth) {
        this.koth = koth;
    }

    @Override
    public String getTitle(Player player) {
        return ChatColor.BLUE + "Schedule: " + koth.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        int slot = 0;
        for (Map.Entry<EventScheduledTime, String> entry : Foxtrot.getInstance().getEventHandler().getEventSchedule().entrySet()) {
            if (entry.getValue().equalsIgnoreCase(koth.getName())) {
                buttons.put(slot++, new ScheduledTimeButton(entry.getKey()));
            }
        }

        buttons.put(45, new AddScheduleButton());
        
        buttons.put(53, new Button() {
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
    public int size(Player player) {
        return 54;
    }

    private class ScheduledTimeButton extends Button {
        private final EventScheduledTime time;

        public ScheduledTimeButton(EventScheduledTime time) {
            this.time = time;
        }

        @Override
        public String getName(Player player) {
            return ChatColor.YELLOW + KOTHScheduleCommand.KOTH_DATE_FORMAT.format(time.toDate());
        }

        @Override
        public List<String> getDescription(Player player) {
            List<String> desc = new ArrayList<>();
            desc.add(ChatColor.GRAY + "Raw: " + ChatColor.WHITE + time.toString());
            desc.add("");
            desc.add(ChatColor.RED + "Click to remove this schedule.");
            return desc;
        }

        @Override
        public Material getMaterial(Player player) {
            return Material.PAPER;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType) {
            Foxtrot.getInstance().getEventHandler().getEventSchedule().remove(time);
            Foxtrot.getInstance().getEventHandler().saveSchedules();
            player.sendMessage(ChatColor.RED + "Removed schedule for " + koth.getName() + " at " + KOTHScheduleCommand.KOTH_DATE_FORMAT.format(time.toDate()));
            new KOTHScheduleEditorMenu(koth).openMenu(player);
        }
    }

    private class AddScheduleButton extends Button {
        @Override
        public String getName(Player player) {
            return ChatColor.GREEN + ChatColor.BOLD.toString() + "Add New Schedule";
        }

        @Override
        public List<String> getDescription(Player player) {
            return new ArrayList<>();
        }

        @Override
        public Material getMaterial(Player player) {
            return Material.NETHER_STAR;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType) {
            player.closeInventory();
            ConversationFactory factory = new ConversationFactory(Foxtrot.getInstance())
                    .withFirstPrompt(new StringPrompt() {
                        @Override
                        public String getPromptText(ConversationContext context) {
                            return ChatColor.YELLOW + "Enter the schedule in format 'DayOfYear Hour:Minute' (e.g. '135 14:30'). Type 'cancel' to exit.";
                        }

                        @Override
                        public Prompt acceptInput(ConversationContext context, String input) {
                            if (input.equalsIgnoreCase("cancel")) return Prompt.END_OF_CONVERSATION;
                            try {
                                EventScheduledTime time = EventScheduledTime.parse(input);
                                Foxtrot.getInstance().getEventHandler().getEventSchedule().put(time, koth.getName());
                                Foxtrot.getInstance().getEventHandler().saveSchedules();
                                context.getForWhom().sendRawMessage(ChatColor.GREEN + "Added schedule for " + koth.getName() + " at " + KOTHScheduleCommand.KOTH_DATE_FORMAT.format(time.toDate()));
                                new KOTHScheduleEditorMenu(koth).openMenu((Player) context.getForWhom());
                            } catch (Exception e) {
                                context.getForWhom().sendRawMessage(ChatColor.RED + "Invalid format. Use 'DayOfYear Hour:Minute'.");
                            }
                            return Prompt.END_OF_CONVERSATION;
                        }
                    }).withLocalEcho(false);
            player.beginConversation(factory.buildConversation(player));
        }
    }
}
