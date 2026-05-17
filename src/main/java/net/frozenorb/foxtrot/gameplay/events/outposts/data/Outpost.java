package net.frozenorb.foxtrot.gameplay.events.outposts.data;

import net.frozenorb.foxtrot.gameplay.events.koth.KOTH;
import net.frozenorb.foxtrot.util.ProgressBarUtil;
import cc.fyre.proton.Proton;
import cc.fyre.proton.hologram.construct.Hologram;
import net.minecraft.util.com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;
import lombok.Setter;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.team.Team;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Outpost {

    @Getter @Setter private ObjectId control = null;
    @Getter private Map<Team, Integer> pointCache = new HashMap<>();
    @Getter @Setter private List<UUID> players = new ArrayList<>();
    @Getter @Setter private List<ObjectId> attacking = new ArrayList<>();
    @Getter @Setter private AtomicDouble percentage = new AtomicDouble(0);
    @Getter @Setter private Location hologramLocation = null;
    @Getter @Setter private long lastMessage = 0;
    @Getter @Setter private long timeCaptured = 0;
    @Getter @Setter private Hologram hologram = null;

    public abstract String getId();
    public abstract ChatColor getDisplayColor();
    public abstract Material getMaterial();
    public abstract int getSlot();
    public abstract List<String> getBenefits();

    public String getDisplayName() {
        return this.getDisplayColor() + ChatColor.BOLD.toString() + this.getId();
    }

    public String getFactionName() {
        return this.getId() + "Outpost";
    }

    public KOTH getKoth() {
        return (KOTH) Foxtrot.getInstance().getEventHandler().getEvent(this.getId() + "-Outpost");
    }

    public List<Team> findAttackers() {
        return this.attacking.stream().map(it -> Foxtrot.getInstance().getTeamHandler().getTeam(it)).filter(Objects::nonNull).collect(Collectors.toList());
    }
    public Team findTeam() {
        return Foxtrot.getInstance().getTeamHandler().getTeam(this.getFactionName());
    }

    public Team findController() {
        return Foxtrot.getInstance().getTeamHandler().getTeam(this.control);
    }

    public OutpostStatus getStatus() {
        if(this.findController() != null) {
            boolean challenging = isChallenging();
            if(!attacking.isEmpty() && challenging) {
                return OutpostStatus.CONTESTED;
            }
            else {
                if(attacking.isEmpty() && challenging)
                    return percentage.get() > 0.0D ? OutpostStatus.CONTROLLED : OutpostStatus.CONTROLLING;

                return !attacking.isEmpty() ? OutpostStatus.NEUTRALIZING : OutpostStatus.CONTROLLED;
            }
        }

        return attacking.isEmpty() ? OutpostStatus.NEUTRAL : (attacking.size() == 1 ? (percentage.get() >= 0.0D ? OutpostStatus.CONTROLLING : OutpostStatus.NEUTRALIZING) : OutpostStatus.CONTESTED);
    }

    public void updatePercentage() {
        OutpostStatus status = getStatus();
        switch(status) {
            case CONTROLLED:
            case CONTROLLING: {
                if(percentage.get() < 100.0D)
                    if(percentage.addAndGet(0.1D) > 100.0D)
                        percentage.set(100.0D);

                if(percentage.get() > 0 && percentage.get() % 20.0 == 0 && percentage.get() < 100.0) {
                    for (Player onlinePlayer : Foxtrot.getInstance().getServer().getOnlinePlayers()) {
                        onlinePlayer.sendMessage("&5[Outposts] &eThe &f" + this.getDisplayName() + " &ehas reached &a" + String.format("%.2f", percentage.get()) + "%&e!");
                    }

                    lastMessage = System.currentTimeMillis();
                }
                break;
            }
            case NEUTRAL:
            case NEUTRALIZING:
                if(percentage.get() > 0.0D) {
                    if(percentage.addAndGet(-0.1D) < 0.0D)
                        percentage.set(0.0D);
                }

                if(percentage.get() > 0 && percentage.get() % 20.0 == 0 && percentage.get() < 100.0) {
                    if(System.currentTimeMillis() - lastMessage < 12000L)
                        return;

                    for (Player onlinePlayer : Foxtrot.getInstance().getServer().getOnlinePlayers()) {
                        onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[Outposts] &eThe &f" + this.getDisplayName() + " &eis being neutralized and is now at &a" + String.format("%.2f", percentage.get()) + "%&e!"));
                    }

                    lastMessage = System.currentTimeMillis();
                }
                break;
        }
    }

    public void updateController() {
        if (this.findAttackers().size() != 1) {
            return;
        }

        setController(new ArrayList<>(this.findAttackers()).get(0));

        if(this.findController() != null) {
            removeAttacker(this.findController());
        }
    }

    public void setController(Team controller) {
        setController(controller, false);
    }

    public void setController(Team controller, boolean onlySet) {
        if(!onlySet) {
            if (controller != null) {
                String[] messages = new String[]{
                        ChatColor.GRAY + "███████",
                        ChatColor.GRAY + "█" + ChatColor.GOLD + "█████" + ChatColor.GRAY + "█" + " " + ChatColor.GOLD + "[Outpost]",
                        ChatColor.GRAY + "█" + ChatColor.GOLD + "█" + ChatColor.GRAY + "███" + ChatColor.GOLD + "█" + ChatColor.GRAY + "█ " + ChatColor.YELLOW + controller.getName() + " has",
                        ChatColor.GRAY + "█" + ChatColor.GOLD + "█" + ChatColor.GRAY + "███" + ChatColor.GOLD + "█" + ChatColor.GRAY + "█" + ChatColor.YELLOW + " gained control of",
                        ChatColor.GRAY + "█" + ChatColor.GOLD + "█" + ChatColor.GRAY + "███" + ChatColor.GOLD + "█" + ChatColor.GRAY + "█ " + ChatColor.translateAlternateColorCodes('&', getDisplayName() + " Outpost"),
                        ChatColor.GRAY + "█" + ChatColor.GOLD + "█████" + ChatColor.GRAY + "█",
                        ChatColor.GRAY + "███████"
                };

                for (String message : messages) {
                    Foxtrot.getInstance().getServer().broadcastMessage(message);
                }

                timeCaptured = System.currentTimeMillis();
            } else if (this.control != null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(" ");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[Outpost] &f" + this.findController().getName(player) + " &ehas lost control of &f" + this.getDisplayName() + " Outpost&e!"));
                    player.sendMessage(" ");
                }
            }
        }

        if (controller != null) {
            this.control = controller.getUniqueId();
        } else {
            this.control = null;
        }
    }

    public long getTimeSinceCapture() {
        if(timeCaptured == 0) {
            return 0;
        }

        return System.currentTimeMillis() - timeCaptured;
    }

    public List<Player> findPlayers() {
        final List<Player> toReturn = new ArrayList<>();

        for (UUID uuid : new ArrayList<>(this.players)) {
            final Player player = Foxtrot.getInstance().getServer().getPlayer(uuid);

            if (player == null || !player.isOnline()) {
                this.players.remove(uuid);
                continue;
            }

            if (!onCapzone(player.getLocation())) {
                this.players.remove(uuid);
                continue;
            }

            toReturn.add(player);
        }

        return toReturn;
    }

    public void updateAttackers() {
        // Cleanup the list of attackers
        this.attacking.removeIf(attacker -> {
            final Team team = Foxtrot.getInstance().getTeamHandler().getTeam(attacker);

            return players.stream().noneMatch(team::isMember);
        });

        for (Player player : this.findPlayers()) {
            final Team team = Foxtrot.getInstance().getTeamHandler().getTeam(player);

            if (this.findController() != null && this.findController().equals(team)) {
                continue;
            }

            addAttacker(team);
        }
    }

    public void checkOld() {
        this.findPlayers().removeIf(uuid -> Foxtrot.getInstance().getOutpostHandler().findOutpost(uuid.getLocation()) != this);

        this.findAttackers().removeIf(attacker -> this.findPlayers().stream().noneMatch(player -> attacker.isMember(player.getUniqueId())));
    }

    public boolean onCapzone(Location location) {
        if (this.getKoth() == null) {
            return false;
        }

        return this.getKoth().onCap(location);
    }

    public void updateHologram() {
        if (hologramLocation == null) return;

        if (hologram == null) {
            hologram = Proton.getInstance().getHologramHandler().createHologram()
                    .at(hologramLocation)
                    .addLines(Arrays.asList("", "", "", "", "", "", "", ""))
                    .build();
            hologram.send();
        }

        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.translateAlternateColorCodes('&', getDisplayName() + " Outpost"));
        lines.add(ChatColor.translateAlternateColorCodes('&', "&8&m------------------------------"));
        lines.add(ChatColor.translateAlternateColorCodes('&', "&7Status: &f" + getStatus().getDisplayName()));
        lines.add(ChatColor.translateAlternateColorCodes('&', "&7Controller: &c" + (this.findController() != null ? this.findController().getName() : "None")));

        if (this.findAttackers().isEmpty()) {
            lines.add(ChatColor.translateAlternateColorCodes('&', "&7Attackers: &cNone"));
        } else {
            lines.add(ChatColor.translateAlternateColorCodes('&', "&7Attacker" + (attacking.size() == 1 ? "" : "s") + ": " + this.findAttackers().stream().map(faction -> "&c" + faction.getName()).collect(Collectors.joining("&f,"))));
        }

        lines.add(ChatColor.translateAlternateColorCodes('&', "&7Percentage: &f" + (percentage.get() == 0.0D ? ChatColor.RED : ChatColor.GREEN) + String.format("%.2f", percentage.get()) + "%"));
        lines.add(ChatColor.translateAlternateColorCodes('&', "&7[" + ProgressBarUtil.getProgressBar(Math.min(percentage.intValue(), 100), 100, 24, "\u25A0", ChatColor.GREEN, ChatColor.RED) + " &7]"));
        lines.add(ChatColor.translateAlternateColorCodes('&', "&8&m------------------------------"));

        hologram.setLines(lines);
    }

    public boolean addAttacker(Team faction) {

        if (this.attacking.contains(faction.getUniqueId())) {
            return false;
        }

        return this.attacking.add(faction.getUniqueId());
    }

    public boolean removeAttacker(Team faction) {
        return this.attacking.remove(faction.getUniqueId());
    }

    public boolean addPlayer(Player player) {
        if (this.players.contains(player.getUniqueId())) {
            return false;
        }

        return this.players.add(player.getUniqueId());
    }

    public boolean removePlayer(Player player) {
        return this.players.remove(player.getUniqueId());
    }

    public boolean hasPlayer(Player player) {
        return this.players.contains(player.getUniqueId());
    }

    public boolean isChallenging() {
        if(this.findController() == null) {
            return false;
        }

        return this.players.stream().anyMatch(player -> Foxtrot.getInstance().getTeamHandler().getTeam(player) != null && Foxtrot.getInstance().getTeamHandler().getTeam(player).equals(this.findController()));
    }
}
