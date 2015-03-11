package net.minelink.ctplus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

public class LogoutTask implements Runnable {

    private int secondsTillLogout;
    private boolean complete = false;
    private final int id;
    private final Location initialLocation;
    private final Player player;
    private static final List<LogoutTask> logoutTasks = new ArrayList<>();

    public int getSecondsTillLogout() {
        return secondsTillLogout;
    }

    public void setSecondsTillLogout(int secondsTillLogout) {
        this.secondsTillLogout = secondsTillLogout;
    }

    public boolean isComplete() {
        return complete;
    }

    public int getId() {
        return id;
    }

    public Location getInitialLocation() {
        return initialLocation;
    }

    public Player getPlayer() {
        return player;
    }

    public static List<LogoutTask> getLogoutTasks() {
        return logoutTasks;
    }

    public static LogoutTask get(Player player) {
        for (LogoutTask logoutTask : logoutTasks) {
            if (logoutTask.getPlayer().equals(player)) {
                return logoutTask;
            }
        }

        return null;
    }

    public LogoutTask(CombatTagPlus plugin, Player player) {
        // Initialize variables
        id = Bukkit.getScheduler().runTaskTimer(plugin, this, 0, 20).getTaskId();
        secondsTillLogout = plugin.getSettings().getLogoutTime();
        initialLocation = player.getLocation();
        this.player = player;
        logoutTasks.add(this);
    }

    @Override
    public void run() {
        // Count down the timer
        secondsTillLogout -= 1;

        // Cancel the task if player is no longer online
        if (!player.isOnline()) {
            Bukkit.getScheduler().cancelTask(id);
            return;
        }

        // Cancel the task if player has moved
        if (initialLocation.getBlockX() != player.getLocation().getBlockX() ||
                initialLocation.getBlockY() != player.getLocation().getBlockY() ||
                initialLocation.getBlockZ() != player.getLocation().getBlockZ()) {
            player.sendMessage(GRAY + "Logout task cancelled due to movement.");
            stopTask();
            return;
        }

        // Safely logout the player once timer is up
        if (secondsTillLogout == 0) {
            complete = true;
            player.kickPlayer(GREEN + "Successfully logged out!");
            Bukkit.getScheduler().cancelTask(id);
            return;
        }

        // Inform player
        player.sendMessage(GRAY + "Logging out in " + RED + secondsTillLogout + GRAY + " seconds");
    }

    public void stopTask() {
        Bukkit.getScheduler().cancelTask(id);
        logoutTasks.remove(this);
    }

}