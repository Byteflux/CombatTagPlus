package net.minelink.ctplus.forcefield; 

import java.util.Collection;
import java.util.HashSet;

public class BorderFinder {
    private BorderFinder() {}

    public static Collection<BlockPos> getBorderPoints(Region region) {
        HashSet<BlockPos> result = new HashSet<>();
        for (BlockPos point : region.getPoints()) {
            getAlongX(point, region, result);
            getAlongZ(point, region, result);
        }
        getBottomOrTop(region.getMin(), region, result);
        getBottomOrTop(region.getMax(), region, result);
        return result;
    }

    private static void getBottomOrTop(BlockPos bottomOrTop, Region region, HashSet<BlockPos> result) {
        for (int x = bottomOrTop.getX(); region.contains(x, bottomOrTop.getY(), bottomOrTop.getZ()); x++) {
            for (int z = bottomOrTop.getZ(); region.contains(x, bottomOrTop.getY(), z); z++) {
                result.add(new BlockPos(x, bottomOrTop.getY(), z, region.getWorld()));
            }
        }
    }

    private static void getAlongX(BlockPos start, Region region, HashSet<BlockPos> result) {
        if (region.contains(start.getX() + 1, start.getY(), start.getZ())) { //We are positive
            for (int x = start.getX(); region.contains(x, start.getY(), start.getZ()); x++) {
                result.add(new BlockPos(x, start.getY(), start.getZ(), region.getWorld()));
            }
        } else { //We are negative or one block
            for (int x = start.getX(); region.contains(x, start.getY(), start.getZ()); x--) {
                result.add(new BlockPos(x, start.getY(), start.getZ(), region.getWorld()));
            }
        }
    }
    
    private static void getAlongZ(BlockPos start, Region region, HashSet<BlockPos> result) {
        if (region.contains(start.getX(), start.getY(), start.getZ() + 1)) { //We are positive
            for (int z = start.getZ(); region.contains(start.getX(), start.getY(), z); z++) {
                result.add(new BlockPos(start.getX(), start.getY(), z, region.getWorld()));
            }
        } else { //We are negative or one block
            for (int z = start.getZ(); region.contains(start.getX(), start.getY(), z); z--) {
                result.add(new BlockPos(start.getX(), start.getY(), z, region.getWorld()));
            }
        }
    }
}