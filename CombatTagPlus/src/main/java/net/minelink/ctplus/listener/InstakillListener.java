package net.minelink.ctplus.listener;

import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.event.CombatLogEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import static com.google.common.base.Preconditions.*;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class InstakillListener implements Listener {
	
	private final Cache<UUID, Instant> keepExp = CacheBuilder.newBuilder()
			.expireAfterWrite(5, TimeUnit.SECONDS)
			.build();
	
	private final CombatTagPlus plugin;
	
	public InstakillListener(CombatTagPlus plugin) {
		this.plugin = checkNotNull(plugin, "Null plugin");
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCombatLog(CombatLogEvent event) {
		if (!plugin.getSettings().instantlyKill()) return; // We aren't configured to instakill
		
		Player player = event.getPlayer();
		
		// Kill the player
		if (event.getReason() == CombatLogEvent.Reason.TAGGED) {
			if (player.hasPermission("essentials.keepxp")) {
				this.keepExp.put(player.getUniqueId(), Instant.now());
			}
			
			player.setHealth(0);
		}
	}
	
	@EventHandler
	private void on(PlayerDeathEvent event) {
		Player player = event.getEntity();
		
		if (this.keepExp.asMap().containsKey(player.getUniqueId())) {
			event.setKeepLevel(true);
			event.setDroppedExp(0);
		}
	}
	
}
