package net.minelink.ctplus.preciousstones;

import org.bukkit.Location;

import net.minelink.ctplus.hook.Hook;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;

public final class PreciousStonesHook implements Hook {

    @Override
    public boolean isPvpEnabledAt(Location loc) {
        return PreciousStones.API().isFieldProtectingArea(FieldFlag.PREVENT_PVP, loc);
    }

}
