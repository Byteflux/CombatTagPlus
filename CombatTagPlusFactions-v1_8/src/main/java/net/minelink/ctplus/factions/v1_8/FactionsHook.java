package net.minelink.ctplus.factions.v1_8;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.FFlag;
import net.minelink.ctplus.forcefield.Region;
import net.minelink.ctplus.hook.Hook;
import org.bukkit.Location;

import java.util.Collection;

public class FactionsHook implements Hook {

    @Override
    public boolean isPvpEnabledAt(Location location) {
        FLocation flocation = new FLocation(location);
        Faction faction = Board.getFactionAt(flocation);
        return faction.getFlag(FFlag.PVP);
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
