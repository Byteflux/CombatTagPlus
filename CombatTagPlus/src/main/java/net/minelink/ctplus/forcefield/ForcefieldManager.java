package net.minelink.ctplus.forcefield;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.MoreExecutors;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * This class is in charge of the new advanced force-field system
 * 
 */
public class ForcefieldManager implements Listener {
    private final CombatTagPlus plugin;
    private final ConcurrentMap<UUID, Collection<BlockPos>> lastShownBlockMap = Maps.newConcurrentMap();
    private final Set<UUID> currentlyProcessing = Sets.newSetFromMap(Maps.<UUID, Boolean>newConcurrentMap());

    public ForcefieldManager(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(final PlayerMoveEvent event) {
        if (event.getFrom().equals(event.getTo())) return; //Don't wanna fire if the player turned his head
        if (currentlyProcessing.contains(event.getPlayer().getUniqueId())) return;
        final UUID playerId = event.getPlayer().getUniqueId();
        if (!plugin.getTagManager().isTagged(playerId)) { //Remove their fake blocks after they are untagged
            if (lastShownBlockMap.containsKey(playerId) && !currentlyProcessing.contains(playerId)) {
                currentlyProcessing.add(event.getPlayer().getUniqueId());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (BlockPos lastShown : lastShownBlockMap.get(playerId)) {
                            event.getPlayer().sendBlockChange(lastShown.toLocation(), lastShown.getTypeAt(), lastShown.getDataAt());
                        }
                        lastShownBlockMap.remove(playerId);
                        currentlyProcessing.remove(event.getPlayer().getUniqueId());
                    }
                }.runTaskAsynchronously(plugin);
            }
            return;
        }
        currentlyProcessing.add(playerId);
        BlockPos pos = new BlockPos(event.getPlayer().getLocation());
        Collection<Region> toUpdate = new HashSet<>();
        for (Region region : plugin.getRegionsToBlock()) {
            if (!region.getWorld().equals(event.getPlayer().getWorld())) continue; //We dont need this one: Yay!
            toUpdate.add(region);
        }
        ForceFieldUpdateRequest request = new ForceFieldUpdateRequest(pos, toUpdate, event.getPlayer(), plugin.getSettings().getForceFieldRadius());
        final ForceFieldUpdateTask task = new ForceFieldUpdateTask(this, request);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        task.addListener(new Runnable() {
            @Override
            public void run() {
                currentlyProcessing.remove(event.getPlayer().getUniqueId());
            }
        }, MoreExecutors.sameThreadExecutor());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent e) {
        lastShownBlockMap.remove(e.getPlayer().getUniqueId());
        currentlyProcessing.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent e) {
        lastShownBlockMap.remove(e.getPlayer().getUniqueId());
        currentlyProcessing.remove(e.getPlayer().getUniqueId());
    }

    public Collection<BlockPos> getLastShownBlocks(Player player) {
        return lastShownBlockMap.get(player);
    }

    public void setLastShownBlocks(Player player, Set<BlockPos> shownBlocks) {
        lastShownBlockMap.put(player.getUniqueId(), shownBlocks);
    }


    private final Map<Region, Collection<BlockPos>> borderCache = Maps.newConcurrentMap(); //Will only be accessed by a single task, so no need for synchronization
    public Collection<BlockPos> getBorders(Region region) {
        if (borderCache.size() > 50) {
            Bukkit.getLogger().severe("[CombatTagPlus] Cache exceeded 50 entries, which should never happen.");
            Bukkit.getLogger().severe("[CombatTagPlus] Clearing cache");
            borderCache.clear();
        }
        if (!borderCache.containsKey(region)) {
            borderCache.put(region, BorderFinder.getBorderPoints(region));
        }
        return borderCache.get(region);
    }
}