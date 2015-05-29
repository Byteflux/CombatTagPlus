/**
 * The MIT License
 * Copyright (c) 2014-2015 Techcable
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.minelink.ctplus.bossbar;

import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.bossbar.fake.FakeDragon;
import net.minelink.ctplus.bossbar.fake.FakeEntity;
import net.minelink.ctplus.bossbar.fake.FakeWither;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class BossBar implements Runnable {
    private BossBar(Player p, CombatTagPlus plugin) {
        this.player = p;
        int version = Packet.getProtocolVersion(p);
        if (version > 5) {
            this.entity = new FakeWither(p);
        } else {
            this.entity = new FakeDragon(p);
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, 3);
    }

    private static final Map<Player, BossBar> bars = new WeakHashMap<Player, BossBar>();
    private final Player player;
    private final FakeEntity entity;
    public static BossBar getBossBar(CombatTagPlus plugin, Player player) {
        if (!bars.containsKey(player)) {
            bars.put(player, new BossBar(player, plugin));
        }
        return bars.get(player);
    }

    //Public Instance Methods

    /**
     * Set the message on the boss bar
     *
     * @param message the message for the player to see on the boss bar
     */
    public void setMessage(String message) {
        entity.setCustomName(message.length() <= 64 ? message : message.substring(0, 63));
        if (isShown()) entity.update();
        trySpawn();
    }

    /**
     * Set how full the boss bar should be
     *
     * @param percent how full the boss bar should be
     */
    public void setPercentage(int percent) {
        percent = Math.max(Math.min(100, percent), 0); //Checking for the errors of others
        entity.setHealth(percent / 100F * entity.getMaxHealth());
        if (isShown()) entity.update();
        trySpawn();
    }

    /**
     * Stop showing the boss bar to this player
     *
     */
    public void stopShowing() {
        if (entity.isSpawned()) {
            entity.despawn();
        }
    }

    /**
     * Return if the boss bar is currently being shown
     * @return
     */
    public boolean isShown() {
        return entity.isSpawned();
    }

    //Internal
    private void trySpawn() {
        if (entity.isSpawned()) return;
        entity.setInvisible(true);
        entity.setHealth(entity.getMaxHealth());
        entity.spawn(calculateLoc());
    }

    @Override
    public void run() {
        if (!entity.isSpawned()) return;
        entity.move(calculateLoc());
    }

    private Location calculateLoc() {
        if (Packet.getProtocolVersion(player) >= 47) {
            return player.getLocation().add(player.getEyeLocation().getDirection().multiply(20));
        } else {
            return player.getLocation().add(0, -300, 0);
        }
    }
}