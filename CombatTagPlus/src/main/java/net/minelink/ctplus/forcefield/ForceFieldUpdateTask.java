package net.minelink.ctplus.forcefield;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.*;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Bukkit;
import org.bukkit.Material;;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ForceFieldUpdateTask extends AbstractFuture implements Runnable, ListenableFuture {

    public ForceFieldUpdateTask(ForcefieldManager manager, ForceFieldUpdateRequest request) {
        this.request = request;
        this.manager = manager;
    }

    public static ListenableFuture<?> schedule(ForcefieldManager manager, ForceFieldUpdateRequest request) {
        ForceFieldUpdateTask task = new ForceFieldUpdateTask(manager, request);
        Bukkit.getScheduler().runTask(CombatTagPlus.getPlugin(CombatTagPlus.class), task);
        return task;
    }
    private final ForcefieldManager manager;
    private final ForceFieldUpdateRequest request;
    @Override
    public void run() {
        Set<BlockPos> shownBlocks = new HashSet<BlockPos>();
        for (Region region : request.getRegionsToUpdate()) {
            for (BlockPos borderPoint : manager.getBorders(region)) {
                for (int y = region.getMin().getY(); y <= region.getMax().getY(); y++) {
                    BlockPos toShow = borderPoint.withY(y);
                    int distance = toShow.distanceSquared(request.getPosition());
                    if (distance <= request.getUpdateRadius()) {
                        shownBlocks.add(toShow);
                    }
                }
            }
        }
        Collection<BlockPos> lastShown = manager.getLastShownBlocks(request.getPlayer());
        if (lastShown == null) lastShown = new HashSet<>();
        for (BlockPos noLongerShown : lastShown) {
            if (shownBlocks.contains(noLongerShown)) continue; //We will show
            request.getPlayerEntity().sendBlockChange(noLongerShown.toLocation(), noLongerShown.getTypeAt().getId(), noLongerShown.getDataAt());
        }
        for (BlockPos toShow : shownBlocks) {
            if (toShow.getTypeAt().isSolid()) continue;
            request.getPlayerEntity().sendBlockChange(toShow.toLocation(), Material.STAINED_GLASS, (byte)14);
        }
        manager.setLastShownBlocks(request.getPlayer(), shownBlocks);
        set(null);
    }

}