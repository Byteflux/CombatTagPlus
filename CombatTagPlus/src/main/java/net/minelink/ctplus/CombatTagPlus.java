package net.minelink.ctplus;

import com.google.common.collect.ImmutableSet;
import net.minelink.ctplus.compat.api.NpcNameGeneratorFactory;
import net.minelink.ctplus.compat.api.NpcPlayerHelper;
import net.minelink.ctplus.forcefield.ForcefieldManager;
import net.minelink.ctplus.forcefield.Region;
import net.minelink.ctplus.hook.Hook;
import net.minelink.ctplus.hook.TownyHook;
import net.minelink.ctplus.listener.ForceFieldListener;
import net.minelink.ctplus.listener.NpcListener;
import net.minelink.ctplus.listener.PlayerHeadsListener;
import net.minelink.ctplus.listener.PlayerListener;
import net.minelink.ctplus.listener.TagListener;
import net.minelink.ctplus.task.SafeLogoutTask;
import net.minelink.ctplus.task.TagUpdateTask;
import net.minelink.ctplus.util.DurationUtils;
import net.minelink.ctplus.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static org.bukkit.ChatColor.*;

public final class CombatTagPlus extends JavaPlugin {

    private final Map<UUID, Player> players = new HashMap<>();

    private final List<Hook> hooks = new ArrayList<>();

    private Settings settings;

    private TagManager tagManager;

    private NpcPlayerHelper npcPlayerHelper;

    private NpcManager npcManager;

    public Settings getSettings() {
        return settings;
    }

    public TagManager getTagManager() {
        return tagManager;
    }

    public NpcPlayerHelper getNpcPlayerHelper() {
        return npcPlayerHelper;
    }

    public NpcManager getNpcManager() {
        return npcManager;
    }

    @Override
    public void onEnable() {
        // Disable plugin if version compatibility check fails
        if (!checkVersionCompatibility()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Load settings
        saveDefaultConfig();

        settings = new Settings(this);
        if (settings.isOutdated()) {
            settings.update();
            getLogger().info("Configuration file has been updated.");
        }

        // Initialize plugin state
        npcManager = new NpcManager(this);
        tagManager = new TagManager(this);

        NpcNameGeneratorFactory.setNameGenerator(new NpcNameGeneratorImpl(this));

        integrateFactions();
        integrateTowny();
        integrateWorldGuard();

        // Build player cache from currently online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.put(player.getUniqueId(), player);
        }

        // Register event listeners
        if (isAdvancedAntiSafezoningSupported()) {
            ForcefieldManager manager = new ForcefieldManager(this);
            Bukkit.getPluginManager().registerEvents(manager, this);
            Bukkit.getLogger().info("[CombatTagPlus] Advanced anti safedzoning is supported");
        } else {
            // If they have the default, warn them that their server will lag a lot without advanced anti-safezoning
            if (getSettings().getForceFieldRadius() >= 100) {
                Bukkit.getLogger().severe(ChatColor.RED + "[CombatTagPlus] You have a very high value for force-field radius");
                Bukkit.getLogger().severe(ChatColor.RED + "[CombatTagPlus] This will cause an insane amount of lag");
                Bukkit.getLogger().severe(ChatColor.RED + "[CombatTagPlus] Either lower the forcefield radius, or activate advanced anti-safezoining");
                Bukkit.getLogger().severe(ChatColor.RED + "[CombatTagPlus] Disabling");
                setEnabled(false);
                return;
            }
            Bukkit.getLogger().warning("[CombatTagPlus] Advanced anti safezoning is unsupported");
            Bukkit.getPluginManager().registerEvents(new ForceFieldListener(this), this);
        }
        Bukkit.getPluginManager().registerEvents(new NpcListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TagListener(this), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlayerHeads")) {
            Bukkit.getPluginManager().registerEvents(new PlayerHeadsListener(this), this);
        }

        // Periodic task for purging unused data
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                getTagManager().purgeExpired();
                TagUpdateTask.purgeFinished();
                SafeLogoutTask.purgeFinished();
            }
        }, 3600, 3600);
    }

    @Override
    public void onDisable() {
        // Clean up player references
        // This also happens for individual players when they disconnect.
        if (players != null) {
            players.clear();
        }
    }

    private boolean checkVersionCompatibility() {
        // Load NMS compatibility helper class
        Class<?> dummyPlayerHelperClass = ReflectionUtils.getCompatClass("NpcPlayerHelperImpl");

        // Warn about incompatibility and return false indicating failure
        if (dummyPlayerHelperClass == null) {
            getLogger().severe("**VERSION ERROR**");
            getLogger().severe("Server API version detected: " + ReflectionUtils.API_VERSION);
            getLogger().severe("This version of CombatTagPlus is not compatible with your CraftBukkit.");
            return false;
        }

        // Helper class was found
        try {
            // Attempt to create a new helper
            npcPlayerHelper = (NpcPlayerHelper) dummyPlayerHelperClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // Fail miserably
            throw new RuntimeException(e);
        }

        // Yay, we're compatible! (hopefully)
        return true;
    }

    private void integrateFactions() {
        if (!getSettings().useFactions()) {
            return;
        }

        // Determine if Factions is loaded
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Factions");
        if (plugin == null) {
            getLogger().info("Factions integration is disabled because it is not loaded.");
            return;
        }

        String[] v = plugin.getDescription().getVersion().split("\\.");
        String version = v[0] + "_" + v[1];

        // Special case for HCF. Use FactionsUUID 1.6 hook
        if (version.compareTo("1_6") < 0) {
            version = "1_6";
        }

        // Determine which hook implementation to use
        String className = "net.minelink.ctplus.factions.v" + version + ".FactionsHook";

        try {
            // Create and add FactionsHook
            addHook((Hook) Class.forName(className).newInstance());
            getLogger().info("Added Factions hook: " + className);
        } catch (Exception e) {
            // Something went wrong, chances are it's a newer, incompatible Factions
            getLogger().warning("**WARNING**");
            getLogger().warning("Failed to enable Factions integration due to errors.");
            getLogger().warning("This is most likely due to a newer Factions.");

            // Let's leave a stack trace in console for reporting
            e.printStackTrace();
        }
    }

    private void integrateTowny() {
        if (!getSettings().useTowny()) {
            return;
        }

        // Determine if Towny is loaded
        if (Bukkit.getPluginManager().isPluginEnabled("Towny")) {
            Hook hook = new TownyHook();
            addHook(hook);
            getLogger().info("Added Towny hook: " + hook.getClass().getCanonicalName());
        } else {
            getLogger().info("Towny integration is disabled because it is not loaded.");
        }
    }

    private void integrateWorldGuard() {
        if (!getSettings().useWorldGuard()) {
            return;
        }

        // Determine if WorldGuard is loaded
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (plugin == null) {
            getLogger().info("WorldGuard integration is disabled because it is not loaded.");
            return;
        }

        String v = plugin.getDescription().getVersion();

        // Determine which hook implementation to use
        String className = "net.minelink.ctplus.worldguard.v" + (v.startsWith("5") ? 5 : 6) + ".WorldGuardHook";

        try {
            // Create and add WorldGuardHook
            addHook((Hook) Class.forName(className).newInstance());
            getLogger().info("Added WorldGuard hook: " + className);
        } catch (Exception e) {
            // Something went wrong, chances are it's a newer, incompatible WorldGuard
            getLogger().warning("**WARNING**");
            getLogger().warning("Failed to enable WorldGuard integration due to errors.");
            getLogger().warning("This is most likely due to a newer WorldGuard.");

            // Let's leave a stack trace in console for reporting
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("ctplusreload")) {
            reloadConfig();
            getSettings().load();
            sender.sendMessage(GREEN + getName() + " config reloaded.");
        } else if (cmd.getName().equals("combattagplus")) {
            if (!(sender instanceof Player)) return false;

            UUID uniqueId = ((Player) sender).getUniqueId();
            Tag tag = getTagManager().getTag(uniqueId);
            if (tag == null || tag.isExpired() || !getTagManager().isTagged(uniqueId)) {
                sender.sendMessage(GREEN + "You are not in combat.");
                return true;
            }

            String duration = DurationUtils.format(tag.getTagDuration());
            sender.sendMessage(RED + duration + GRAY + " remaining on your combat timer.");
        } else if (cmd.getName().equals("ctpluslogout")) {
            if (!(sender instanceof Player)) return false;

            // Do nothing if player is already logging out
            Player player = (Player) sender;
            if (SafeLogoutTask.hasTask(player)) return false;

            // Attempt to start a new logout task
            SafeLogoutTask.run(this, player);
        }

        return true;
    }

    public boolean addPlayer(Player player) {
        return players.put(player.getUniqueId(), player) != null;
    }

    public boolean removePlayer(Player player) {
        return players.remove(player.getUniqueId()) != null;
    }

    public Player getPlayer(UUID playerId) {
        return players.get(playerId);
    }

    public boolean addHook(Hook hook) {
        return hooks.add(hook);
    }

    public boolean removeHook(Hook hook) {
        return hooks.remove(hook);
    }

    public boolean isPvpEnabledAt(Location location) {
        for (Hook hook : hooks) {
            if (!hook.isPvpEnabledAt(location)) {
                return false;
            }
        }
        return true;
    }

    private final Object regionLock = new Object();
    private ImmutableSet<Region> regionsToBlock = null;
    public Collection<Region> getRegionsToBlock() {
        if (System.currentTimeMillis() % 10 * 60 * 1000 == 0) { //Refresh every 10 minutes
            if (!Bukkit.isPrimaryThread()) { //Dont Block the current thread!
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        refreshRegionsToBlock();
                    }
                }.runTask(this);
            } else {
                refreshRegionsToBlock();
            }
        }
        if (regionsToBlock == null) {
            synchronized (regionLock) {
                if (regionsToBlock == null) {
                    refreshRegionsToBlock();
                    return regionsToBlock;
                }
            }
        }
        return regionsToBlock;
    }

    private void refreshRegionsToBlock() {
        if (!Bukkit.isPrimaryThread()) { //Just in case some idiot nulled out the cache
            final Object lock = new Object();
            new BukkitRunnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        refreshRegionsToBlock();
                        lock.notifyAll();
                    }
                }
            }.runTask(this);
            while (true) {
                synchronized (lock) {
                    try {
                        lock.wait();
                        break;
                    } catch (InterruptedException ex) {}
                }
            }
        }
        synchronized (regionLock) {
            regionsToBlock = null;
            ImmutableSet.Builder builder = ImmutableSet.builder();
            for (Hook hook : hooks) {
                if (hook.isAdvancedAntiSafezoningSupported()) {
                    builder.addAll(hook.getRegionsToBlock());
                }
            }
            regionsToBlock = builder.build();
        }
    }

    public boolean isAdvancedAntiSafezoningSupported() {
        for (Hook hook : hooks) {
            if (hook.isAdvancedAntiSafezoningSupported()) return true;
        }
        return false;
    }

}
