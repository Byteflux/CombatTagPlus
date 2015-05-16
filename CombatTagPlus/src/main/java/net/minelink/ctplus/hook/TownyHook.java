package net.minelink.ctplus.hook;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import net.minelink.ctplus.forcefield.Region;
import org.bukkit.Location;

import java.util.Collection;

public final class TownyHook implements Hook {

    @Override
    public boolean isPvpEnabledAt(Location loc) {
        TownyWorld world;
        try {
            world = TownyUniverse.getDataSource().getWorld(loc.getWorld().getName());
        } catch (NotRegisteredException ignore) {
            return true;
        }

        TownBlock townBlock = null;
        try {
            townBlock = world.getTownBlock(Coord.parseCoord(loc));
        } catch (NotRegisteredException ignore) {

        }

        return !CombatUtil.preventPvP(world, townBlock);
    }


    @Override
    public boolean isAdvancedAntiSafezoningSupported() {
        return false;
    }

    @Override
    public Collection<Region> getRegionsToBlock() {
        throw new UnsupportedOperationException();
    }

}
