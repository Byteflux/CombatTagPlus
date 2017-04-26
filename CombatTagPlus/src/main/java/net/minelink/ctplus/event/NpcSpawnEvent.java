package net.minelink.ctplus.event;

import net.minelink.ctplus.Npc;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class NpcSpawnEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Npc npc;

    public NpcSpawnEvent(Npc npc) {
        this.npc = npc;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Npc getNpc() {
        return npc;
    }
}
