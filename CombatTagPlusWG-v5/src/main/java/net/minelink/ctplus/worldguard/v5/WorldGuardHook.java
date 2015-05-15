package net.minelink.ctplus.worldguard.v5;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minelink.ctplus.forcefield.Region;
import net.minelink.ctplus.hook.Hook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class WorldGuardHook implements Hook {

    @Override
    public boolean isPvpEnabledAt(Location loc) {
        return WGBukkit.getRegionManager(loc.getWorld()).getApplicableRegions(loc).allows(DefaultFlag.PVP);
    }


    @Override
    public boolean isAdvancedAntiSafezoningSupported() {
        return true;
    }

    @Override
    public Collection<Region> getRegionsToBlock() {
        Set<Region> regionsToBlock = new HashSet<>();
        for (World world : Bukkit.getWorlds()) {
            RegionManager manger = WGBukkit.getRegionManager(world);
            for (ProtectedRegion region : manger.getRegions().values()) {
                WorldGuardRegion wrapper = new WorldGuardRegion(world, region);
                regionsToBlock.add(wrapper);
            }
        }
        return regionsToBlock;
    }
}
