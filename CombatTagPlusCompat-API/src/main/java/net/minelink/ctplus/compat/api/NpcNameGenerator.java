package net.minelink.ctplus.compat.api;

import java.util.UUID;

import org.bukkit.entity.Player;

public interface NpcNameGenerator {

    public String generateName(Player player);

    public UUID generateUUID(Player player);

}
