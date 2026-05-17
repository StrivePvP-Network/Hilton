package net.frozenorb.foxtrot.server.voucher.menu;

import cc.fyre.proton.menu.Button;
import cc.fyre.proton.menu.Menu;
import cc.fyre.proton.util.ItemBuilder;
import cc.fyre.proton.util.TimeUtils;
import lombok.AllArgsConstructor;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.server.voucher.Voucher;
import net.frozenorb.foxtrot.util.CC;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

@AllArgsConstructor
public class VoucherAjustesMenu extends Menu {

    private Voucher voucher;

    @Override
    public String getTitle(Player player) {
        return "Voucher Settings";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(13, new Button() {
            @Override
            public String getName(Player player) {
                return ChatColor.YELLOW + ChatColor.BOLD.toString() + "Set Expiration";
            }

            @Override
            public List<String> getDescription(Player player) {
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(CC.translate("&eCurrent Expiration: &f" + (voucher.getExpirationTime() == 0 ? "None" : new Date(voucher.getExpirationTime()).toLocaleString())));
                lore.add("");
                lore.add(ChatColor.GRAY + "Click to set a new expiration time (e.g. 7d, 30d).");
                lore.add(ChatColor.GRAY + "Type 'none' to remove expiration.");
                return lore;
            }

            @Override
            public Material getMaterial(Player player) {
                return Material.WATCH;
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType) {
                player.closeInventory();
                
                ConversationFactory factory = new ConversationFactory(Foxtrot.getInstance())
                        .withModality(true)
                        .withPrefix(new NullConversationPrefix())
                        .withFirstPrompt(new StringPrompt() {
                            @Override
                            public String getPromptText(ConversationContext context) {
                                return "§eType the duration (e.g. 7d, 30d, 1h) or 'none':";
                            }

                            @Override
                            public Prompt acceptInput(ConversationContext context, String input) {
                                if (input.equalsIgnoreCase("none")) {
                                    voucher.setExpirationTime(0);
                                    player.sendMessage(ChatColor.GREEN + "Removed expiration.");
                                } else {
                                    try {
                                        long seconds = TimeUtils.parseTime(input);
                                        voucher.setExpirationTime(System.currentTimeMillis() + (seconds * 1000));
                                        player.sendMessage(ChatColor.GREEN + "Set expiration to: " + new Date(voucher.getExpirationTime()).toLocaleString());
                                    } catch (Exception e) {
                                        player.sendMessage(ChatColor.RED + "Invalid duration format. Try again.");
                                        return this;
                                    }
                                }
                                
                                Foxtrot.getInstance().getVoucherHandler().saveData();
                                new VoucherAjustesMenu(voucher).openMenu(player);
                                return Prompt.END_OF_CONVERSATION;
                            }
                        })
                        .withLocalEcho(false)
                        .withEscapeSequence("/cancel")
                        .withTimeout(60);
                
                player.beginConversation(factory.buildConversation(player));
            }
        });

        return buttons;
    }
}
