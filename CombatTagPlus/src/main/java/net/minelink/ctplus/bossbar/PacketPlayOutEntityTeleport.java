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

import org.bukkit.Location;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static net.minelink.ctplus.bossbar.Reflection.*;

/**
* Created by Nicholas Schlabach on 4/16/2015.
*/
public class PacketPlayOutEntityTeleport extends Packet {
    public static final Class<?> PACKET_CLASS = getNmsClass("PacketPlayOutEntityTeleport");
    static {
        Field[] fields = PACKET_CLASS.getDeclaredFields();
        teleportEntityIdField = fields[0];
        teleportXField = fields[1];
        teleportYField = fields[2];
        teleportZField = fields[3];
        teleportYawField = fields[4];
        teleportPitchField = fields[5];
        teleportOnGroundField = fields[6];
    }

    public PacketPlayOutEntityTeleport(int entityId, Location l, boolean onGround) {
        Object handle = callConstructor(constructor);
        this.handle = handle;
        setField(teleportEntityIdField, handle, entityId);
        setField(teleportXField, handle, toFixedPoint(l.getX()));
        setField(teleportYField, handle, toFixedPoint(l.getY()));
        setField(teleportZField, handle, toFixedPoint(l.getZ()));
        setField(teleportYawField, handle, toByteAngle(l.getYaw()));
        setField(teleportPitchField, handle, toByteAngle(l.getPitch()));
        if (teleportOnGroundField != null) {
            setField(teleportOnGroundField, handle, onGround);
        }
    }

    private static final Constructor constructor = makeConstructor(PACKET_CLASS);
    private static Field teleportEntityIdField;
    private static Field teleportXField;
    private static Field teleportYField;
    private static Field teleportZField;
    private static Field teleportYawField;
    private static Field teleportPitchField;
    private static Field teleportOnGroundField;

    private final Object handle;
    public Object getHandle() {
        return handle;
    }

    @Override
    public Class<?> getPacketClass() {
        return PACKET_CLASS;
    }
}
