package net.minelink.ctplus.forcefield;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkRegion implements Region {
    public ChunkRegion(String name, World world, Set<ChunkPos> chunks) {
        this.name = name;
        this.chunks = chunks;
        this.world = world;
    }
    private final String name;
    private final Set<ChunkPos> chunks;
    private final World world;


    @Override
    public boolean contains(BlockPos point) {
        return chunks.contains(point.getChunkPos());
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return contains(new BlockPos(x, y, z, getWorld()));
    }

    @Override
    public Collection<BlockPos> getPoints() {
        Set<BlockPos> points = new HashSet<>();
        for (ChunkPos chunkPos : chunks) {
            ImmutableMap<CornerType, BlockPos> corners = getCorners(chunkPos);
            for (Map.Entry<CornerType, BlockPos> corner : corners.entrySet()) {
                if (isNotAdjacent(corner.getValue(), corner.getKey())) {
                    points.add(corner.getValue());
                }
            }
        }
        return points;
    }

    private boolean isNotAdjacent(BlockPos corner, CornerType type) {
        if (chunks.contains(type.getNorthSouthNext(corner).getChunkPos())) return false;
        if (chunks.contains(type.getEastWestNext(corner).getChunkPos())) return false;
        return true;
    }

    private ImmutableMap<CornerType, BlockPos> getCorners(ChunkPos chunk) {
        BlockPos northWest = new BlockPos(chunk.getAbsoluteX(15), 0, chunk.getAbsoluteZ(0), chunk.getWorld());
        BlockPos northEast = new BlockPos(chunk.getAbsoluteX(15), 0, chunk.getAbsoluteZ(15), chunk.getWorld());
        BlockPos southWest = new BlockPos(chunk.getAbsoluteX(0), 0, chunk.getAbsoluteZ(0), chunk.getWorld());
        BlockPos southEast = new BlockPos(chunk.getAbsoluteX(0), 0, chunk.getAbsoluteZ(15), chunk.getWorld());
        return ImmutableMap.of(CornerType.NORTH_WEST, northWest, CornerType.NORTH_EAST, northEast, CornerType.SOUTH_WEST, southWest, CornerType.SOUTH_EAST, southEast);
    }


    private enum CornerType {
        NORTH_WEST(true, false),
        NORTH_EAST(true, true),
        SOUTH_WEST(false, false),
        SOUTH_EAST(false, true);
        private final boolean north, east;

        CornerType(boolean north, boolean east) {
            this.north = north;
            this.east = east;
        }

        public BlockPos getNorthSouthNext(BlockPos corner) {
            if (north) {
                return corner.withX(corner.getX() + 1);
            } else {
                return corner.withX(corner.getX() - 1);
            }
        }

        public BlockPos getEastWestNext(BlockPos corner) {
            if (east) {
                return corner.withZ(corner.getZ() + 1);
            } else {
                return corner.withZ(corner.getZ() - 1);
            }
        }
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public BlockPos getMin() {
        BlockPos min = null;
        for (BlockPos point : getPoints()) {
            int x = min == null ? point.getX() : Math.min(point.getX(), min.getX());
            int y = min == null ? point.getY() : Math.min(point.getY(), min.getX());
            int z = min == null ? point.getZ() : Math.min(point.getZ(), min.getX());
            min = new BlockPos(x, y, z, point.getWorld());
        }
        return min;
    }

    @Override
    public BlockPos getMax() {
        BlockPos max = null;
        for (BlockPos point : getPoints()) {
            int x = max == null ? point.getX() : Math.max(point.getX(), max.getX());
            int y = max == null ? point.getY() : Math.max(point.getY(), max.getX());
            int z = max == null ? point.getZ() : Math.max(point.getZ(), max.getX());
            max = new BlockPos(x, y, z, point.getWorld());
        }
        return max;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return false;
        if (other instanceof ChunkRegion) {
            return name.equals(((ChunkRegion) other).name);
        }
        return false;
    }
}
