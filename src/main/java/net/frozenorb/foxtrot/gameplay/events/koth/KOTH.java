package net.frozenorb.foxtrot.gameplay.events.koth;

import cc.fyre.modsuite.mod.ModHandler;
import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.common.location.ApolloBlockLocation;
import com.lunarclient.apollo.module.waypoint.Waypoint;
import com.lunarclient.apollo.module.waypoint.WaypointModule;
import com.lunarclient.apollo.player.ApolloPlayer;
import lombok.Getter;
import lombok.Setter;
import net.frozenorb.foxtrot.Foxtrot;
import net.frozenorb.foxtrot.gameplay.events.Event;
import net.frozenorb.foxtrot.gameplay.events.EventType;
import net.frozenorb.foxtrot.gameplay.events.events.EventActivatedEvent;
import net.frozenorb.foxtrot.gameplay.events.events.EventCapturedEvent;
import net.frozenorb.foxtrot.gameplay.events.events.EventDeactivatedEvent;
import net.frozenorb.foxtrot.gameplay.events.koth.events.EventControlTickEvent;
import net.frozenorb.foxtrot.gameplay.events.koth.events.KOTHControlLostEvent;
import net.frozenorb.foxtrot.team.Team;
import net.frozenorb.foxtrot.util.Cuboid;
import net.frozenorb.foxtrot.util.InventoryUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import java.awt.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class KOTH implements Event {

    @Getter
    private String name;
    @Getter
    private BlockVector capLocation;
    @Getter
    private String world;
    @Getter
    private int capDistance;
    @Getter
    private int capTime;
    @Getter
    private boolean hidden = false;
    @Getter
    @Setter
    private boolean mini = false;
    @Getter
    @Setter
    boolean active;

    @Getter
    @Setter
    private int pointsReward = 0;
    @Getter
    @Setter
    private int antiCleanRadius = 0;
    @Getter
    @Setter
    private String kothColor = "&9";
    @Getter
    private List<ItemStack> loot = new ArrayList<>();
    @Getter
    @Setter
    private Cuboid captureZone;

    @Getter
    private transient String currentCapper;
    @Getter
    private transient int remainingCapTime;
    @Getter
    @Setter
    private transient boolean terminate;

    @Getter
    public boolean koth = true;

    @Getter
    private EventType type = EventType.KOTH;

    public KOTH(String name, Location location) {
        this.name = name;
        this.capLocation = location.toVector().toBlockVector();
        this.world = location.getWorld().getName();
        this.capDistance = 3;
        this.capTime = 60 * 15;
        this.terminate = false;

        Foxtrot.getInstance().getEventHandler().getEvents().add(this);
        Foxtrot.getInstance().getEventHandler().saveEvents();
    }

    public void setLocation(Location location) {
        this.capLocation = location.toVector().toBlockVector();
        this.world = location.getWorld().getName();
        Foxtrot.getInstance().getEventHandler().saveEvents();
    }

    public void setCapDistance(int capDistance) {
        this.capDistance = capDistance;
        Foxtrot.getInstance().getEventHandler().saveEvents();
    }

    public java.awt.Color getWaypointColor() {
        switch (this.getName()) {
            case "Hell":
            case "Conquest-Red":
                return java.awt.Color.RED;
            case "Conquest-Yellow":
                return java.awt.Color.YELLOW;
            case "Conquest-Green":
                return java.awt.Color.GREEN;
            case "Conquest-Middle":
            case "Citadel":
            case "End":
                return Color.MAGENTA;
            default:
                return Color.blue;
        }
    }

    public ChatColor getWaypointChatColor() {
        switch (this.getName()) {
            case "Hell":
            case "Conquest-Red":
                return ChatColor.RED;
            case "Conquest-Yellow":
                return ChatColor.YELLOW;
            case "Conquest-Green":
                return ChatColor.GREEN;
            case "Conquest-Middle":
            case "Citadel":
            case "End":
                return ChatColor.DARK_PURPLE;
            default:
                return ChatColor.AQUA;
        }
    }

    public void setCapTime(int capTime) {
        int oldCapTime = this.capTime;
        this.capTime = capTime;

        if (this.remainingCapTime > capTime) {
            this.remainingCapTime = capTime;
        } else if (remainingCapTime == oldCapTime) { // this will catch the time going up
            this.remainingCapTime = capTime;
        }

        Foxtrot.getInstance().getEventHandler().saveEvents();
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        Foxtrot.getInstance().getEventHandler().saveEvents();
    }

    public boolean activate() {
        if (active) {
            return (false);
        }

        Foxtrot.getInstance().getServer().getPluginManager().callEvent(new EventActivatedEvent(this));

        this.active = true;
        this.currentCapper = null;
        this.remainingCapTime = this.capTime;
        this.terminate = false;

        sendWaypoints();

        return (true);
    }

    public boolean deactivate() {
        if (!active) {
            return (false);
        }

        Foxtrot.getInstance().getServer().getPluginManager().callEvent(new EventDeactivatedEvent(this));

        this.active = false;
        this.currentCapper = null;
        this.remainingCapTime = this.capTime;
        this.terminate = false;

        removeWaypoints();

        return (true);
    }

    public void startCapping(Player player) {
        if (currentCapper != null) {
            resetCapTime();
        }

        this.currentCapper = player.getName();
        this.remainingCapTime = capTime;
    }

    public boolean finishCapping() {
        Player capper = Foxtrot.getInstance().getServer().getPlayerExact(currentCapper);

        if (capper == null) {
            resetCapTime();
            return (false);
        }

        EventCapturedEvent event = new EventCapturedEvent(this, capper);
        Foxtrot.getInstance().getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            resetCapTime();
            return (false);
        }

        reward(capper);
        deactivate();
        return (true);
    }

    public void reward(Player player) {
        Team team = Foxtrot.getInstance().getTeamHandler().getTeam(player);

        if (pointsReward > 0 && team != null) {
            team.setPoints(team.getPoints() + pointsReward);
            team.sendMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.YELLOW + "Your faction received "
                    + ChatColor.BLUE + pointsReward + ChatColor.YELLOW + " points for capturing " + ChatColor.BLUE
                    + getName() + ChatColor.YELLOW + ".");
        }

        ItemStack kothSign = Foxtrot.getInstance().getServerHandler().generateKOTHSign(getName(),
                team == null ? player.getName() : team.getName(), EventType.KOTH);
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), kothSign);
        } else {
            player.getInventory().addItem(kothSign);
        }

        for (ItemStack item : loot) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            } else {
                player.getInventory().addItem(item);
            }
        }
    }

    public void resetCapTime() {
        Foxtrot.getInstance().getServer().getPluginManager().callEvent(new KOTHControlLostEvent(this));

        this.currentCapper = null;
        this.remainingCapTime = capTime;

        if (terminate) {
            deactivate();
            Foxtrot.getInstance().getServer().broadcastMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.BLUE
                    + getName() + ChatColor.YELLOW + " has been terminated.");
        }
    }

    @Override
    public void tick() {
        if (currentCapper != null) {
            Player capper = Foxtrot.getInstance().getServer().getPlayerExact(currentCapper);

            if (capper == null || !onCap(capper.getLocation()) || capper.isDead()
                    || capper.getGameMode() == GameMode.CREATIVE || ModHandler.INSTANCE.isInVanish(capper.getUniqueId())
                    || Foxtrot.getInstance().getPvPTimerMap().hasTimer(capper.getUniqueId())
                    || (Foxtrot.getInstance().getTeamHandler().getTeam(capper) != null && Foxtrot.getInstance().getTeamHandler().getTeam(capper).getStrikes() >= 3)) {
                resetCapTime();
            } else if (Foxtrot.getInstance().getTeamHandler().getTeam(capper) == null && isMini()) {
                capper.sendMessage(ChatColor.RED + "You must be in a faction to capture a Mini KOTH!");
                resetCapTime();
            } else {
                if (remainingCapTime % 60 == 0 && remainingCapTime > 1 && !isHidden()) {

                    Team team = Foxtrot.getInstance().getTeamHandler().getTeam(capper);

                    if (team != null) {
                        for (Player player : Foxtrot.getInstance().getServer().getOnlinePlayers()) {
                            if (team.isMember(player.getUniqueId()) && capper != player) {
                                player.sendMessage(ChatColor.GOLD + "[KingOfTheHill]" + ChatColor.YELLOW
                                        + " Your faction is controlling " + ChatColor.BLUE + getName()
                                        + ChatColor.YELLOW + ".");
                            }
                        }
                    }
                }

                if (remainingCapTime % 10 == 0 && remainingCapTime > 1 && !isHidden()) {
                    capper.sendMessage(ChatColor.GOLD + "[KingOfTheHill]" + ChatColor.YELLOW + " Attempting to control "
                            + ChatColor.BLUE + getName() + ChatColor.YELLOW + ".");
                }

                if (remainingCapTime <= 0) {
                    finishCapping();
                } else {
                    Foxtrot.getInstance().getServer().getPluginManager().callEvent(new EventControlTickEvent(this));
                }

                this.remainingCapTime--;
            }
        } else {
            List<Player> onCap = new ArrayList<>();

            for (Player player : Foxtrot.getInstance().getServer().getOnlinePlayers()) {
                if (onCap(player.getLocation())) {
                    Team team = Foxtrot.getInstance().getTeamHandler().getTeam(player);
                    if (team != null && team.getStrikes() >= 3) {
                        if (player.getTicksLived() % 100 == 0) {
                            player.sendMessage(ChatColor.RED + "Your faction has been disqualified from KOTHs due to 3 or more strikes!");
                        }
                        continue;
                    }
                    if (Foxtrot.getInstance().getBattlePassHandler() != null) {
                        Foxtrot.getInstance().getBattlePassHandler().useProgress(player.getUniqueId(), progress -> {
                            progress.setAttemptCaptureKoth(true);
                            progress.requiresSave();

                            Foxtrot.getInstance().getBattlePassHandler().checkCompletionsAsync(player);
                        });
                    }
                }

                Team team = Foxtrot.getInstance().getTeamHandler().getTeam(player);
                if (team != null && team.getStrikes() >= 3) {
                    continue;
                }

                if (onCap(player.getLocation()) && !player.isDead() && player.getGameMode() == GameMode.SURVIVAL
                        && !ModHandler.INSTANCE.isInModMode(player.getUniqueId())
                        && !Foxtrot.getInstance().getPvPTimerMap().hasTimer(player.getUniqueId())) {
                    onCap.add(player);
                }
            }

            Collections.shuffle(onCap);

            if (onCap.size() != 0) {
                startCapping(onCap.get(0));
            }
        }
    }

    public boolean onCap(Location location) {
        if (!location.getWorld().getName().equalsIgnoreCase(world)) {
            return (false);
        }

        if (captureZone != null) {
            return captureZone.contains(location);
        }

        int xDistance = Math.abs(location.getBlockX() - capLocation.getBlockX());
        int yDistance = Math.abs(location.getBlockY() - capLocation.getBlockY());
        int zDistance = Math.abs(location.getBlockZ() - capLocation.getBlockZ());

        return xDistance <= capDistance && yDistance <= 5 && zDistance <= capDistance;
    }

    private void sendWaypoints() {
        Waypoint waypoint = Waypoint.builder()
                .name(getName())
                .location(ApolloBlockLocation.builder()
                        .world(world)
                        .x(capLocation.getBlockX())
                        .y(capLocation.getBlockY())
                        .z(capLocation.getBlockZ())
                        .build())
                .color(getWaypointColor())
                .preventRemoval(false)
                .hidden(false)
                .build();

        WaypointModule waypointModule = Apollo.getModuleManager().getModule(WaypointModule.class);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Optional<ApolloPlayer> apolloPlayer = Apollo.getPlayerManager().getPlayer(player.getUniqueId());
            apolloPlayer.ifPresent(p -> waypointModule.displayWaypoint(p, waypoint));
        }
    }

    private void removeWaypoints() {
        WaypointModule waypointModule = Apollo.getModuleManager().getModule(WaypointModule.class);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Optional<ApolloPlayer> apolloPlayer = Apollo.getPlayerManager().getPlayer(player.getUniqueId());
            apolloPlayer.ifPresent(p -> waypointModule.removeWaypoint(p, getName()));
        }
    }

}