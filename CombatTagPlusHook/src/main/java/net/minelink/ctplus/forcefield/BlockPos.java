package net.minelink.ctplus.forcefield;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class BlockPos {

    public BlockPos(int x, int y, int z, World world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public BlockPos(Location l) {
        this(l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld());
    }

    private final int x, y, z;
    private final World world;

    public Location toLocation() {
        return new Location(getWorld(), getX(), getY(), getZ());
    }

    public int distanceSquared(BlockPos other) {
        Preconditions.checkArgument(other.getWorld().equals(getWorld()), "Can't compare the distances of different worlds");
        return square(x - other.x) + square(y - other.y) + square(z - other.z);
    }

    public Material getTypeAt() {
        return Material.getMaterial(getWorld().getBlockTypeIdAt(getX(), getY(), getZ()));
    }

    public byte getDataAt() {
        return getWorld().getBlockAt(getX(), getY(), getZ()).getData();
    }

    private final ChunkPos chunkPos = new ChunkPos(getX() >> 4, getZ() >> 4, getWorld());

    private static int square(int i) {
        return i * i;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public World getWorld() {
        return world;
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public BlockPos withX(int x) {
        return new BlockPos(x, y, z, world);
    }

    public BlockPos withY(int y) {
        return new BlockPos(x, y, z, world);
    }

    public BlockPos withZ(int z) {
        return new BlockPos(x, y, z, world);
    }

    public BlockPos withWorld(World world) {
        if (world.equals(this.getWorld())) return this;
        return new BlockPos(x, y, z, world);
    }
}