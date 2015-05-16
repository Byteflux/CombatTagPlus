package net.minelink.ctplus.factions.v2_7;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MFlag;
import com.massivecraft.massivecore.ps.PS;
import net.minelink.ctplus.forcefield.ChunkPos;
import net.minelink.ctplus.forcefield.ChunkRegion;
import net.minelink.ctplus.forcefield.Region;
import net.minelink.ctplus.hook.Hook;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FactionsHook implements Hook {

    @Override
    public boolean isPvpEnabledAt(Location location) {
        PS ps = PS.valueOf(location);
        Faction faction = BoardColl.get().getFactionAt(ps);
        return faction.getFlag(MFlag.ID_PVP);
    }

    @Override
    public boolean isAdvancedAntiSafezoningSupported() {
        return false;
    }

    @Override
    public Collection<Region> getRegionsToBlock() {
        Set<Region> toBlock = new HashSet<>();
        Faction safezone = FactionColl.get().getSafezone();
        Set<ChunkPos> checked = new HashSet<>();
        for (PS chunkPS : BoardColl.get().getChunks(safezone)) {
            Chunk chunk = chunkPS.asBukkitChunk();
            if (checked.contains(new ChunkPos(chunk.getX(), chunk.getZ(), chunk.getWorld()))) continue;
            Set<ChunkPos> adjacentChunks = new HashSet<>();
            getAdjacentChunks(chunk.getX(), chunk.getX(), adjacentChunks, safezone);
            checked.addAll(adjacentChunks);
            ChunkRegion region = new ChunkRegion(safezone.getName(), chunk.getWorld(), adjacentChunks);
        }
        return toBlock;
    }

    private void getAdjacentChunks(int x, int z, Set<ChunkPos> adjacent, Faction faction) {
        PS ps = PS.valueOf(x, z);
        if (!BoardColl.get().getFactionAt(ps).equals(faction)) return;
        getAdjacentChunks(x + 1, z, adjacent, faction);
        getAdjacentChunks(x - 1, z, adjacent, faction);
        getAdjacentChunks(x, z + 1, adjacent, faction);
        getAdjacentChunks(x, z - 1, adjacent, faction);
        adjacent.add(new ChunkPos(x, z, ps.asBukkitWorld()));
    }

}
