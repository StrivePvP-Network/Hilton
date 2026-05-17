package net.frozenorb.foxtrot.gameplay.events.koth.menu;

import cc.fyre.proton.menu.Button;
import cc.fyre.proton.menu.Menu;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.gameplay.events.koth.KOTH;
import net.frozenorb.foxtrot.gameplay.events.koth.commands.koth.KOTHSetCapzoneCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KOTHAjustesMenu extends Menu {

    private final KOTH koth;

    public KOTHAjustesMenu(KOTH koth) {
        this.koth = koth;
    }

    @Override
    public String getTitle(Player player) {
        return ChatColor.BLUE + "Settings: " + koth.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(10, new EditTimeButton());
        buttons.put(12, new EditPointsButton());
        buttons.put(14, new EditScheduleButton());
        buttons.put(16, new EditAntiCleanButton());
        buttons.put(22, new EditColorButton());
        buttons.put(24, new EditCapzoneButton());
        buttons.put(26, new EditLootButton());

        buttons.put(size(player) - 9, new Button() {
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
                new KOTHEditorMenu().openMenu(player);
            }
        });

        return buttons;
    }

    @Override
    public int size(Player player) {
        return 36;
    }

    private class EditTimeButton extends Button {
        @Override
        public String getName(Player player) {
            return ChatColor.YELLOW + "Edit Capture Time";
        }

        @Override
        public List<String> getDescription(Player player) {
            List<String> desc = new ArrayList<>();
            desc.add(ChatColor.GRAY + "Current: " + ChatColor.WHITE + (koth.getCapTime() / 60) + "m");
            desc.add("");
            desc.add(ChatColor.YELLOW + "Click to edit.");
            return desc;
        }

        @Override
        public Material getMaterial(Player player) {
            return Material.WATCH;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType) {
            player.closeInventory();
            ConversationFactory factory = new ConversationFactory(Foxtrot.getInstance())
                    .withFirstPrompt(new StringPrompt() {
                        @Override
                        public String getPromptText(ConversationContext context) {
                            return ChatColor.YELLOW
                                    + "Enter the new capture time in minutes (e.g. 15). Type 'cancel' to exit.";
                        }

                        @Override
                        public Prompt acceptInput(ConversationContext context, String input) {
                            if (input.equalsIgnoreCase("cancel"))
                                return Prompt.END_OF_CONVERSATION;
                            try {
                                int minutes = Integer.parseInt(input);
                                koth.setCapTime(minutes * 60);
                                context.getForWhom().sendRawMessage(
                                        ChatColor.GREEN + "Set capture time to " + minutes + " minutes.");
                                new KOTHAjustesMenu(koth).openMenu((Player) context.getForWhom());
                            } catch (NumberFormatException e) {
                                context.getForWhom().sendRawMessage(ChatColor.RED + "Invalid number.");
                            }
                            return Prompt.END_OF_CONVERSATION;
                        }
                    }).withLocalEcho(false);
            player.beginConversation(factory.buildConversation(player));
        }
    }

    private class EditPointsButton extends Button {
        @Override
        public String getName(Player player) {
            return ChatColor.YELLOW + "Edit Points Reward";
        }

        @Override
        public List<String> getDescription(Player player) {
            List<String> desc = new ArrayList<>();
            desc.add(ChatColor.GRAY + "Current: " + ChatColor.WHITE + koth.getPointsReward());
            desc.add("");
            desc.add(ChatColor.YELLOW + "Click to edit.");
            return desc;
        }

        @Override
        public Material getMaterial(Player player) {
            return Material.GOLD_INGOT;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType) {
            player.closeInventory();
            ConversationFactory factory = new ConversationFactory(Foxtrot.getInstance())
                    .withFirstPrompt(new StringPrompt() {
                        @Override
                        public String getPromptText(ConversationContext context) {
                            return ChatColor.YELLOW + "Enter the new points reward. Type 'cancel' to exit.";
                        }

                        @Override
                        public Prompt acceptInput(ConversationContext context, String input) {
                            if (input.equalsIgnoreCase("cancel"))
                                return Prompt.END_OF_CONVERSATION;
                            try {
                                int points = Integer.parseInt(input);
                                koth.setPointsReward(points);
                                Foxtrot.getInstance().getEventHandler().saveEvents();
                                context.getForWhom()
                                        .sendRawMessage(ChatColor.GREEN + "Set points reward to " + points + ".");
                                new KOTHAjustesMenu(koth).openMenu((Player) context.getForWhom());
                            } catch (NumberFormatException e) {
                                context.getForWhom().sendRawMessage(ChatColor.RED + "Invalid number.");
                            }
                            return Prompt.END_OF_CONVERSATION;
                        }
                    }).withLocalEcho(false);
            player.beginConversation(factory.buildConversation(player));
        }
    }

    private class EditScheduleButton extends Button {
        @Override
        public String getName(Player player) {
            return ChatColor.YELLOW + "Edit Schedule";
        }

        @Override
        public List<String> getDescription(Player player) {
            List<String> desc = new ArrayList<>();
            desc.add(ChatColor.GRAY + "Configure activation times.");
            desc.add("");
            desc.add(ChatColor.YELLOW + "Click to open scheduler.");
            return desc;
        }

        @Override
        public Material getMaterial(Player player) {
            return Material.BOOK;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType) {
            new KOTHScheduleEditorMenu(koth).openMenu(player);
        }
    }

    private class EditAntiCleanButton extends Button {
        @Override
        public String getName(Player player) {
            return ChatColor.YELLOW + "Edit Anti-Clean Radius";
        }

        @Override
        public List<String> getDescription(Player player) {
            List<String> desc = new ArrayList<>();
            desc.add(ChatColor.GRAY + "Current: " + ChatColor.WHITE + koth.getAntiCleanRadius());
            desc.add("");
            desc.add(ChatColor.YELLOW + "Click to edit.");
            return desc;
        }

        @Override
        public Material getMaterial(Player player) {
            return Material.POTION;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType) {
            player.closeInventory();
            ConversationFactory factory = new ConversationFactory(Foxtrot.getInstance())
                    .withFirstPrompt(new StringPrompt() {
                        @Override
                        public String getPromptText(ConversationContext context) {
                            return ChatColor.YELLOW + "Enter the new anti-clean radius. Type 'cancel' to exit.";
                        }

                        @Override
                        public Prompt acceptInput(ConversationContext context, String input) {
                            if (input.equalsIgnoreCase("cancel"))
                                return Prompt.END_OF_CONVERSATION;
                            try {
                                int radius = Integer.parseInt(input);
                                koth.setAntiCleanRadius(radius);
                                Foxtrot.getInstance().getEventHandler().saveEvents();
                                context.getForWhom()
                                        .sendRawMessage(ChatColor.GREEN + "Set anti-clean radius to " + radius + ".");
                                new KOTHAjustesMenu(koth).openMenu((Player) context.getForWhom());
                            } catch (NumberFormatException e) {
                                context.getForWhom().sendRawMessage(ChatColor.RED + "Invalid number.");
                            }
                            return Prompt.END_OF_CONVERSATION;
                        }
                    }).withLocalEcho(false);
            player.beginConversation(factory.buildConversation(player));
        }
    }

    private class EditColorButton extends Button {
        @Override
        public String getName(Player player) {
            return ChatColor.YELLOW + "Edit KOTH Color";
        }

        @Override
        public List<String> getDescription(Player player) {
            List<String> desc = new ArrayList<>();
            String color = koth.getKothColor() == null ? "&9" : koth.getKothColor();
            desc.add(ChatColor.GRAY + "Current: " + ChatColor.translateAlternateColorCodes('&', color) + "This Color");
            desc.add("");
            desc.add(ChatColor.YELLOW + "Click to edit.");
            return desc;
        }

        @Override
        public Material getMaterial(Player player) {
            return Material.WOOL;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType) {
            player.closeInventory();
            ConversationFactory factory = new ConversationFactory(Foxtrot.getInstance())
                    .withFirstPrompt(new StringPrompt() {
                        @Override
                        public String getPromptText(ConversationContext context) {
                            return ChatColor.YELLOW + "Enter the new KOTH color code (e.g. &9). Type 'cancel' to exit.";
                        }

                        @Override
                        public Prompt acceptInput(ConversationContext context, String input) {
                            if (input.equalsIgnoreCase("cancel"))
                                return Prompt.END_OF_CONVERSATION;
                            koth.setKothColor(input);
                            Foxtrot.getInstance().getEventHandler().saveEvents();
                            context.getForWhom().sendRawMessage(ChatColor.GREEN + "Set KOTH color to "
                                    + ChatColor.translateAlternateColorCodes('&', input) + "this color" + ChatColor.GREEN + ".");
                            new KOTHAjustesMenu(koth).openMenu((Player) context.getForWhom());
                            return Prompt.END_OF_CONVERSATION;
                        }
                    }).withLocalEcho(false);
            player.beginConversation(factory.buildConversation(player));
        }
    }

    private class EditCapzoneButton extends Button {
        @Override
        public String getName(Player player) {
            return ChatColor.YELLOW + "Set Capture Zone (Wand)";
        }

        @Override
        public List<String> getDescription(Player player) {
            List<String> desc = new ArrayList<>();
            desc.add(ChatColor.GRAY + "Use a selection wand to");
            desc.add(ChatColor.GRAY + "define the exact capzone.");
            desc.add("");
            if (koth.getCaptureZone() != null) {
                desc.add(ChatColor.GREEN + "Currently defined by selection.");
                desc.add(
                        ChatColor.GRAY + "Volume: " + ChatColor.WHITE + koth.getCaptureZone().getVolume() + " blocks.");
            } else {
                desc.add(ChatColor.RED + "Currently defined by radius (" + koth.getCapDistance() + ").");
            }
            desc.add("");
            desc.add(ChatColor.YELLOW + "Click to receive wand.");
            return desc;
        }

        @Override
        public Material getMaterial(Player player) {
            return Material.GOLD_HOE;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType) {
            player.closeInventory();
            KOTHSetCapzoneCommand.setCapzone(player, koth);
        }
    }

    private class EditLootButton extends Button {
        @Override
        public String getName(Player player) {
            return ChatColor.YELLOW + "Edit Loot";
        }

        @Override
        public List<String> getDescription(Player player) {
            List<String> desc = new ArrayList<>();
            desc.add(ChatColor.GRAY + "Configure the loot table");
            desc.add(ChatColor.GRAY + "using a drag & drop menu.");
            desc.add("");
            desc.add(ChatColor.YELLOW + "Click to open editor.");
            return desc;
        }

        @Override
        public Material getMaterial(Player player) {
            return Material.CHEST;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType) {
            new KOTHLootEditorMenu(koth).openMenu(player);
        }
    }
}
