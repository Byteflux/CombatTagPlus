package net.minelink.ctplus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public final class SafeLogoutTask extends BukkitRunnable {

    private final static List<SafeLogoutTask> tasks = new ArrayList<>();

    private final CombatTagPlus plugin;

    private final UUID playerId;

    private final Location loc;

    private final long logoutTime;

    private final int taskId;

    private int remainingSeconds = Integer.MAX_VALUE;

    private boolean finished = false;

    SafeLogoutTask(CombatTagPlus plugin, Player player, long logoutTime) {
        this.plugin = plugin;
        this.playerId = player.getUniqueId();
        this.loc = player.getLocation();
        this.logoutTime = logoutTime;

        // Run the task every few ticks for accuracy
        this.taskId = this.runTaskTimer(plugin, 0, 5).getTaskId();

        tasks.add(this);
    }

    static SafeLogoutTask get(Player player) {
        // Attempt to find the task that is running for the specified player
        UUID uuid = player.getUniqueId();
        for (SafeLogoutTask safeLogoutTask : tasks) {
            if (safeLogoutTask.getPlayerId().equals(uuid)) {
                return safeLogoutTask;
            }
        }

        return null;
    }

    static void run(CombatTagPlus plugin, Player player) {
        // Do nothing if player already has a task
        SafeLogoutTask safeLogoutTask = SafeLogoutTask.get(player);
        if (safeLogoutTask != null) return;

        // Calculate logout time
        long logoutTime = System.currentTimeMillis() + (plugin.getSettings().getLogoutWaitTime() * 1000);

        // Create the new task
        new SafeLogoutTask(plugin, player, logoutTime);
    }

    private int getRemainingSeconds() {
        long currentTime = System.currentTimeMillis();
        return logoutTime > currentTime ? NumberConversions.ceil((logoutTime - currentTime) / 1000D) : 0;
    }

    private boolean hasMoved(Player player) {
        Location l = player.getLocation();
        return loc.getWorld() != l.getWorld() || loc.getBlockX() != l.getBlockX() ||
                loc.getBlockY() != l.getBlockY() || loc.getBlockZ() != l.getBlockZ();
    }

    public boolean isFinished() {
        return finished;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    @Override
    public void run() {
        // Cancel the task if player is no longer online
        Player player = plugin.getPlayer(playerId);
        if (player == null) {
            cancel();
            return;
        }

        // Cancel the task if player has moved
        if (hasMoved(player)) {
            player.sendMessage(RED + "Logout cancelled due to movement.");
            cancel();
            return;
        }

        // Safely logout the player once timer is up
        int remainingSeconds = getRemainingSeconds();
        if (remainingSeconds <= 0) {
            finished = true;
            player.kickPlayer(GREEN + "You were logged out safely.");
            cancel();
            return;
        }

        // Inform player
        if (remainingSeconds < this.remainingSeconds) {
            String remaining = DurationUtils.format(remainingSeconds);
            player.sendMessage(GRAY + "Logging out safely in " + AQUA + remaining + GRAY + " ...");
            this.remainingSeconds = remainingSeconds;
        }
    }

    public void cancel() {
        // Cancel logout task
        Bukkit.getScheduler().cancelTask(taskId);

        // Remove from cache
        // Must be removed like this otherwise it CAN be exploited
        tasks.remove(this);
    }

}