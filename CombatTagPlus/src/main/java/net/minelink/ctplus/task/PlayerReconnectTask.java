package net.minelink.ctplus.task;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.UUID;

import org.bukkit.util.NumberConversions;

import net.minelink.ctplus.CombatTagPlus;

public class PlayerReconnectTask implements Runnable {

    private final CombatTagPlus plugin;

    private final UUID playerUUID;

    private final NpcDespawnTask npcDespawnTask;

    private long time = -1;

    private int taskId;

    public PlayerReconnectTask(CombatTagPlus plugin, NpcDespawnTask npcDespawnTask, UUID playerUUID) {
        this.plugin = checkNotNull(plugin, "Null plugin");
        this.npcDespawnTask = checkNotNull(npcDespawnTask, "Null despawnTask");
        this.playerUUID = checkNotNull(playerUUID, "Null UUID");
        
    }

    public long getTime() {
        return time != -1 ? time : npcDespawnTask.getTime() + plugin.getSettings().getReconnectionTime() * 1000;
    }

    public int getRemainingSeconds() {
        long currentTime = System.currentTimeMillis();
        return getTime() > currentTime ? NumberConversions.ceil((getTime() - currentTime) / 1000D) : 0;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public NpcDespawnTask getNpcDspawnTask() {
        return npcDespawnTask;
    }

    public void start() {
        taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, this, 1, 1).getTaskId();
    }

    public void stop() {
        plugin.getServer().getScheduler().cancelTask(taskId);
    }

    @Override
    public void run() {
        // Do nothing if NPC should not despawn yet
        if (getTime() > System.currentTimeMillis()) {
            return;
        }

        // Cancel the task to let the player join the server
        plugin.getNpcManager().cancelReconnectTask(playerUUID);
    }

}
