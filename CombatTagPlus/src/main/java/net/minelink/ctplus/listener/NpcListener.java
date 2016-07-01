package net.minelink.ctplus.listener;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.Npc;
import net.minelink.ctplus.event.NpcDespawnEvent;
import net.minelink.ctplus.event.NpcDespawnReason;
import net.minelink.ctplus.task.PlayerReconnectTask;
import net.minelink.ctplus.task.SafeLogoutTask;
import net.minelink.ctplus.util.DurationUtils;

public final class NpcListener implements Listener {

    private final CombatTagPlus plugin;

    public NpcListener(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void spawnNpc(PlayerQuitEvent event) {
        // Do nothing if player is not combat tagged and NPCs only spawn if tagged
        Player player = event.getPlayer();

        // Do nothing if player is dead
        if (player.isDead()) return;

        boolean isTagged = plugin.getTagManager().isTagged(player.getUniqueId());
        if (!isTagged && !plugin.getSettings().alwaysSpawn()) return;

        // Do nothing if player is not within enabled world
        if (plugin.getSettings().getDisabledWorlds().contains(player.getWorld().getName())) return;

        // Do nothing if a player logs off in combat in a WorldGuard protected region
        if (!plugin.getHookManager().isPvpEnabledAt(player.getLocation())) return;

        // Do nothing if player has permission
        if (player.hasPermission("ctplus.bypass.tag")) return;

        // Do nothing if player has safely logged out
        if (SafeLogoutTask.isFinished(player)) return;

        // Kill player if configuration states so
        if (isTagged && plugin.getSettings().instantlyKill()) {
            player.setHealth(0);
            return;
        }

        // Spawn a new NPC
        plugin.getNpcManager().spawn(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void despawnNpc(PlayerLoginEvent event) {
        // Do nothing if player can't login
        if(event.getResult() != Result.ALLOWED) return;
        
        // Check if player has a remaining task and kick him with message
        if(plugin.getNpcManager().hasReconnectTask(event.getPlayer().getUniqueId())) {
            PlayerReconnectTask task = plugin.getNpcManager().getReconnectTask(event.getPlayer().getUniqueId());
            if(task != null) {
                String remaining = DurationUtils.format(task.getRemainingSeconds());
                event.disallow(Result.KICK_OTHER, plugin.getSettings().getReconnectionKickMessage().replace("{remaining}", remaining));
                return;
            }
        }
        
        // Attempt to despawn NPC
        Npc npc = plugin.getNpcManager().getSpawnedNpc(event.getPlayer().getUniqueId());
        if (npc != null) {
            plugin.getNpcManager().despawn(npc);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void despawnNpc(PlayerDeathEvent event) {
        // Do nothing if player is not a NPC
        Player player = event.getEntity();
        if (!plugin.getNpcPlayerHelper().isNpc(player)) return;

        // Fetch NPC using the actual player identity
        UUID id = plugin.getNpcPlayerHelper().getIdentity(player).getId();
        final Npc npc = plugin.getNpcManager().getSpawnedNpc(id);
        if (npc == null) return;

        // NPC died, remove player's combat tag
        plugin.getTagManager().untag(id);

        // Despawn NPC on the next tick
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getNpcManager().despawn(npc, NpcDespawnReason.DEATH);
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void updateDespawnTime(EntityDamageByEntityEvent event) {
        // Do nothing if we are not to update NPC despawn time on hit
        if (!plugin.getSettings().resetDespawnTimeOnHit()) return;

        // Do nothing if entity damaged is not a player
        if (!(event.getEntity() instanceof Player)) return;

        // Do nothing if player damaged is not a NPC
        Player player = (Player) event.getEntity();
        if (!plugin.getNpcPlayerHelper().isNpc(player)) return;

        // Update the NPC despawn time
        UUID npcId = plugin.getNpcPlayerHelper().getIdentity(player).getId();
        Npc npc = plugin.getNpcManager().getSpawnedNpc(npcId);
        if (plugin.getNpcManager().hasDespawnTask(npc)) {
            long despawnTime = System.currentTimeMillis() + plugin.getSettings().getNpcDespawnMillis();
            plugin.getNpcManager().getDespawnTask(npc).setTime(despawnTime);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void syncOffline(PlayerDeathEvent event) {
        // Do nothing if player is not a NPC
        final Player player = event.getEntity();
        if (!plugin.getNpcPlayerHelper().isNpc(player)) return;

        // NPC died, remove player's combat tag
        plugin.getTagManager().untag(player.getUniqueId());

        // Save NPC player data on next tick
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getNpcPlayerHelper().syncOffline(player);
            }
        });
    }

    @EventHandler
    public void syncOffline(AsyncPlayerPreLoginEvent event) {
        // Do nothing if login is already disallowed
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        // Do nothing if there is no spawned NPC for this player
        final UUID playerId = event.getUniqueId();
        if (!plugin.getNpcManager().npcExists(playerId)) return;

        // Synchronously save the NPC's player data
        Future<?> future = Bukkit.getScheduler().callSyncMethod(plugin, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Npc npc = plugin.getNpcManager().getSpawnedNpc(playerId);
                if (npc == null) return null;

                plugin.getNpcPlayerHelper().syncOffline(npc.getEntity());
                return null;
            }
        });

        // Wait for the save to complete
        // TODO: There must be a better way of doing this?
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void syncOffline(NpcDespawnEvent event) {
        Npc npc = event.getNpc();

        // Save player data when the NPC despawns
        Player player = plugin.getPlayerCache().getPlayer(npc.getIdentity().getId());
        if (player == null) {
            plugin.getNpcPlayerHelper().syncOffline(npc.getEntity());
            return;
        }

        // Copy NPC player data to online player
        Player npcPlayer = npc.getEntity();
        player.setMaximumAir(npcPlayer.getMaximumAir());
        player.setRemainingAir(npcPlayer.getRemainingAir());
        player.setHealthScale(npcPlayer.getHealthScale());
        player.setMaxHealth(npcPlayer.getMaxHealth());
        player.setHealth(npcPlayer.getHealth());
        player.setTotalExperience(npcPlayer.getTotalExperience());
        player.setFoodLevel(npcPlayer.getFoodLevel());
        player.setExhaustion(npcPlayer.getExhaustion());
        player.setSaturation(npcPlayer.getSaturation());
        player.setFireTicks(npcPlayer.getFireTicks());
        player.getInventory().setContents(npcPlayer.getInventory().getContents());
        player.getInventory().setArmorContents(npcPlayer.getInventory().getArmorContents());
        player.addPotionEffects(npcPlayer.getActivePotionEffects());
    }

}
