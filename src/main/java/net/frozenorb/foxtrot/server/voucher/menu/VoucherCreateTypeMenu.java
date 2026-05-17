package net.frozenorb.foxtrot.server.voucher.menu;

import cc.fyre.proton.menu.Button;
import cc.fyre.proton.menu.Menu;
import cc.fyre.proton.util.UUIDUtils;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.server.voucher.Voucher;
import net.frozenorb.foxtrot.util.CC;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class VoucherCreateTypeMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "Choose Voucher Type";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(11, new Button() {
            @Override
            public String getName(Player player) {
                return ChatColor.YELLOW + ChatColor.BOLD.toString() + "Rank Voucher";
            }

            @Override
            public List<String> getDescription(Player player) {
                return Collections.singletonList(ChatColor.GRAY + "Create a voucher that gives a rank.");
            }

            @Override
            public Material getMaterial(Player player) {
                return Material.DIAMOND;
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType) {
                player.closeInventory();
                startConversation(player, "rank");
            }
        });

        buttons.put(13, new Button() {
            @Override
            public String getName(Player player) {
                return ChatColor.AQUA + ChatColor.BOLD.toString() + "Bundle Voucher";
            }

            @Override
            public List<String> getDescription(Player player) {
                return Collections.singletonList(ChatColor.GRAY + "Create a voucher that gives a bundle.");
            }

            @Override
            public Material getMaterial(Player player) {
                return Material.CHEST;
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType) {
                player.closeInventory();
                startConversation(player, "bundle");
            }
        });

        buttons.put(15, new Button() {
            @Override
            public String getName(Player player) {
                return ChatColor.GREEN + ChatColor.BOLD.toString() + "Amount Voucher";
            }

            @Override
            public List<String> getDescription(Player player) {
                return Collections.singletonList(ChatColor.GRAY + "Create a Buycraft/Tebex coupon voucher.");
            }

            @Override
            public Material getMaterial(Player player) {
                return Material.GOLD_INGOT;
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType) {
                player.closeInventory();
                startConversation(player, "amount");
            }
        });

        return buttons;
    }

    private void startConversation(Player player, String type) {
        ConversationFactory factory = new ConversationFactory(Foxtrot.getInstance())
                .withModality(true)
                .withPrefix(new NullConversationPrefix())
                .withFirstPrompt(new VoucherCreatePrompt(type))
                .withLocalEcho(false)
                .withEscapeSequence("/cancel")
                .withTimeout(60)
                .thatExcludesNonPlayersWithMessage("Go away!");
        
        player.beginConversation(factory.buildConversation(player));
        player.sendMessage(ChatColor.YELLOW + "Creating " + type + " voucher. Type details or /cancel.");
    }
}

class VoucherCreatePrompt extends StringPrompt {
    private String type;
    private int step = 0;
    
    private UUID target;
    private String name;
    private String rankName;
    private String duration;
    private String bundleId;
    private int amount;

    public VoucherCreatePrompt(String type) {
        this.type = type;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        switch (step) {
            case 0: return "§eType the player name (target):";
            case 1: return "§eType the Display Name (e.g. §f&6&lVIP Voucher§e):";
            case 2:
                if (type.equals("rank")) return "§eType the Rank Name (e.g. §fVIP§e):";
                if (type.equals("bundle")) return "§eType the Bundle ID:";
                if (type.equals("amount")) return "§eType the Amount ($):";
            case 3:
                if (type.equals("rank")) return "§eType the Duration (e.g. §fperm §eor §f30d§e):";
            default: return "";
        }
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();
        
        if (step == 0) {
            target = UUIDUtils.uuid(input);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Invalid player name. Try again.");
                return this;
            }
            step++;
            return this;
        }
        
        if (step == 1) {
            name = ChatColor.translateAlternateColorCodes('&', input);
            step++;
            return this;
        }
        
        if (step == 2) {
            if (type.equals("rank")) {
                rankName = input;
                step++;
                return this;
            } else if (type.equals("bundle")) {
                bundleId = input;
                finish(player);
                return Prompt.END_OF_CONVERSATION;
            } else if (type.equals("amount")) {
                try {
                    amount = Integer.parseInt(input);
                    finish(player);
                    return Prompt.END_OF_CONVERSATION;
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid number. Try again.");
                    return this;
                }
            }
        }
        
        if (step == 3 && type.equals("rank")) {
            duration = input;
            finish(player);
            return Prompt.END_OF_CONVERSATION;
        }

        return Prompt.END_OF_CONVERSATION;
    }
    
    private void finish(Player player) {
        Voucher voucher = new Voucher(target, name, player.getName());
        if (type.equals("rank")) {
            voucher.setRank(true);
            voucher.setRankName(rankName);
            voucher.setRankDuration(duration);
        } else if (type.equals("bundle")) {
            voucher.setBundle(true);
            voucher.setBundleID(bundleId);
        } else if (type.equals("amount")) {
            voucher.setAmount(amount);
        }
        
        player.sendMessage(ChatColor.GREEN + "Successfully created " + type + " voucher for " + UUIDUtils.name(target) + "!");
        Foxtrot.getInstance().getVoucherHandler().saveData();
    }
}
