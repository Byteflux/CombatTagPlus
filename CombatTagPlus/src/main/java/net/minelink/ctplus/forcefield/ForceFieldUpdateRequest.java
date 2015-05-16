package net.minelink.ctplus.forcefield;

import org.bukkit.entity.Player;

import java.util.Collection;

public class ForceFieldUpdateRequest {
    private final BlockPos position;
    private final Collection<Region> regionsToUpdate;
    private final Player player;
    private final int updateRadius;
    private volatile boolean completed = false;

    public ForceFieldUpdateRequest(BlockPos position, Collection<Region> regionsToUpdate, Player player, int updateRadius) {
        this.position = position;
        this.regionsToUpdate = regionsToUpdate;
        this.player = player;
        this.updateRadius = updateRadius;
    }

    public void setCompleted() {
        completed = true;
    }

    public Player getPlayerEntity() {
        return getPlayer();
    }

    public BlockPos getPosition() {
        return position;
    }

    public Collection<Region> getRegionsToUpdate() {
        return regionsToUpdate;
    }

    public Player getPlayer() {
        return player;
    }

    public int getUpdateRadius() {
        return updateRadius;
    }

    public boolean isCompleted() {
        return completed;
    }
}