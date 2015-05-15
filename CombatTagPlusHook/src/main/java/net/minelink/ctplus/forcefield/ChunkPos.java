package net.minelink.ctplus.forcefield;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * An immutable position of a chunk
 */
public class ChunkPos {
    private final int x, z;
    private final World world;

    public ChunkPos(int x, int z, World world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    public int getAbsoluteX(int relativeX) {
        return fromRelative(getX(), relativeX);
    }
    public int getAbsoluteZ(int absoluteZ) {
        return fromRelative(getZ(), absoluteZ);
    }
    public static ChunkPos fromChunk(Chunk chunk) {
        return new ChunkPos(chunk.getX(), chunk.getZ(), chunk.getWorld());
    }
    public static ChunkPos fromLocation(Location l) {
        return new ChunkPos(l.getBlockX() >> 4, l.getBlockZ() >> 4, l.getWorld());
    }
    public static int toRelative(int absolute) {
        return absolute & 0xF; //First 16 bits
    }
    public static int fromRelative(int chunk, int relative) {
        return (chunk << 4) | (relative & 0xF);
    }

    public boolean isLoaded() {
        return getWorld().isChunkLoaded(getX(), getZ());
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public World getWorld() {
        return world;
    }
}