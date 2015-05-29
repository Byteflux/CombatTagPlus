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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static net.minelink.ctplus.bossbar.Reflection.*;

public class PacketPlayOutEntityMetadata extends Packet {
    private static final Class<?> PACKET_CLASS = getNmsClass("PacketPlayOutEntityMetadata");
    static {
        for (int i = 0; i < PACKET_CLASS.getDeclaredFields().length; i++) {
            Field field = PACKET_CLASS.getDeclaredFields()[i];
            switch (i) {
                case 0 :
                    entityIdField = field;
                    break;
                case 1 :
                    dataWatcherValuesField = field;
                    break;
                default :
                    throw new RuntimeException("Unknown field index " + i);
            }
        }
    }
    private static Field entityIdField;
    private static Field dataWatcherValuesField;
    private static final Constructor constructor = makeConstructor(PACKET_CLASS);

    @Override
    public Object getHandle() {
        return handle;
    }

    private final Object handle;
    public PacketPlayOutEntityMetadata(int entityId, WrappedDataWatcher watcher) {
        this.handle = callConstructor(constructor);
        setField(entityIdField, handle, entityId);
        setField(dataWatcherValuesField, handle, watcher.toHandleList());
    }

    @Override
    public Class<?> getPacketClass() {
        return PACKET_CLASS;
    }
}
