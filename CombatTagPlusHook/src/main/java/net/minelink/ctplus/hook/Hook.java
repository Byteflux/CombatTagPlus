package net.minelink.ctplus.hook;

import org.bukkit.Location;
import org.bukkit.World;

import net.minelink.ctplus.forcefield.Region;

import java.util.Collection;

public interface Hook {

    boolean isPvpEnabledAt(Location loc);

    public boolean isAdvancedAntiSafezoningSupported();

    // These methods throw an UnsupportedOperationException if the above method returns false

    public Collection<Region> getRegionsToBlock(); // This method must be thread safe

}
