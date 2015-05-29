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

import net.minelink.ctplus.bossbar.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static net.minelink.ctplus.bossbar.Reflection.*;

public class PacketPlayOutSpawnEntityLiving extends Packet {

    public static final Class<?> PACKET_CLASS = getNmsClass("PacketPlayOutSpawnEntityLiving");
    static {
        Field[] fields = PACKET_CLASS.getDeclaredFields();
        entityIdField = fields[0];
        entityTypeField = fields[1];
        entityXField = fields[2];
        entityYField = fields[3];
        entityZField = fields[4];
        entityVelocityXField = fields[5];
        entityVelocityYField = fields[6];
        entityVelocityZField = fields[7];
        entityYawField = fields[8];
        entityPitchField = fields[9];
        entityHeadPitchField = fields[10];
        entityDataWatcherField = fields[11];
    }
    private static Field entityIdField;
    private static Field entityTypeField;
    private static Field entityXField;
    private static Field entityYField;
    private static Field entityZField;
    private static Field entityVelocityXField;
    private static Field entityVelocityYField;
    private static Field entityVelocityZField;
    private static Field entityYawField;
    private static Field entityPitchField;
    private static Field entityHeadPitchField;
    private static Field entityDataWatcherField;

    public PacketPlayOutSpawnEntityLiving(int entityId, byte entityType, Location location, Vector bukkitVelocity, WrappedDataWatcher watcher) {
        setField(entityIdField, getHandle(), entityId);
        setField(entityTypeField, getHandle(), (int)entityType);
        setField(entityXField, getHandle(), toFixedPoint(location.getX()));
        setField(entityYField, getHandle(), toFixedPoint(location.getY()));
        setField(entityZField, getHandle(), toFixedPoint(location.getZ()));
        int[] velocity = doVelocityMagic(bukkitVelocity.getX(), bukkitVelocity.getY(), bukkitVelocity.getZ());
        setField(entityVelocityXField, getHandle(), velocity[0]);
        setField(entityVelocityYField, getHandle(), velocity[1]);
        setField(entityVelocityZField, getHandle(), velocity[2]);
        setField(entityYawField, getHandle(), toByteAngle(location.getYaw()));
        setField(entityPitchField, getHandle(), toByteAngle(location.getPitch()));
        setField(entityHeadPitchField, getHandle(), toByteAngle(location.getPitch())); //Meh
        setField(entityDataWatcherField, getHandle(), watcher.getHandle());
    }

    @Override
    public Class<?> getPacketClass() {
        return PACKET_CLASS;
    }
}
