package net.minelink.ctplus.worldguard.v6;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minelink.ctplus.forcefield.BlockPos;
import net.minelink.ctplus.forcefield.Region;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.Collection;

public class WorldGuardRegion implements Region {
    private final World world;
    private final ProtectedRegion region;

    public WorldGuardRegion(World world, ProtectedRegion region) {
        this.world = world;
        this.region = region;
    }

    @Override
    public boolean contains(BlockPos point) {
        return contains(point.getX(), point.getY(), point.getZ());
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return region.contains(x, y, z);
    }

    @Override
    public Collection<BlockPos> getPoints() {
        return Collections2.transform(region.getPoints(), new Function<BlockVector2D, BlockPos>() {
            @Nullable
            @Override
            public BlockPos apply(BlockVector2D input) {
                return new BlockPos(input.getBlockX(), getMin().getY(), input.getBlockZ(), getWorld());
            }
        });
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public BlockPos getMin() {
        BlockVector min = region.getMinimumPoint();
        return new BlockPos(min.getBlockX(), min.getBlockY(), min.getBlockZ(), getWorld());
    }

    @Override
    public BlockPos getMax() {
        BlockVector max = region.getMaximumPoint();
        return new BlockPos(max.getBlockX(), max.getBlockY(), max.getBlockZ(), getWorld());
    }

    @Override
    public String getName() {
        return region.getId();
    }

    @Override
    public int hashCode() {
        return region.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return false;
        if (other instanceof WorldGuardRegion) {
            return region.equals(((WorldGuardRegion) other).region);
        }
        return false;
    }
}
