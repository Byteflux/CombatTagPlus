package net.minelink.ctplus.compat.v1_9_R2;

import java.util.Map;

import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.MinecraftServer;
import net.minecraft.server.v1_9_R2.PlayerInteractManager;
import net.minecraft.server.v1_9_R2.WorldServer;
import net.minelink.ctplus.compat.api.NpcIdentity;
import net.minelink.ctplus.compat.api.NpcNameGeneratorFactory;

public final class NpcPlayer extends EntityPlayer {

    private NpcIdentity identity;

    private NpcPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
    }

    public NpcIdentity getNpcIdentity() {
        return identity;
    }

    public static NpcPlayer valueOf(Player player) {
        MinecraftServer minecraftServer = MinecraftServer.getServer();
        WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();
        PlayerInteractManager playerInteractManager = new PlayerInteractManager(worldServer);
        GameProfile gameProfile = new GameProfile(NpcNameGeneratorFactory.getNameGenerator().generateUUID(player), NpcNameGeneratorFactory.getNameGenerator().generateName(player));

        for (Map.Entry<String, Property> entry: ((CraftPlayer) player).getProfile().getProperties().entries()) {
            gameProfile.getProperties().put(entry.getKey(), entry.getValue());
        }

        NpcPlayer npcPlayer = new NpcPlayer(minecraftServer, worldServer, gameProfile, playerInteractManager);
        npcPlayer.identity = new NpcIdentity(player);

        new NpcPlayerConnection(npcPlayer);

        return npcPlayer;
    }

}
