package net.minelink.ctplus.forcefield;

import org.bukkit.World;

import java.util.Collection;

public interface Region {
    public boolean contains(BlockPos point);
    public boolean contains(int x, int y, int z);
    public Collection<BlockPos> getPoints();
    public World getWorld();
    public BlockPos getMin();
    public BlockPos getMax();
    public String getName();
}