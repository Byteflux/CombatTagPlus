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

import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static net.minelink.ctplus.bossbar.Reflection.*;

public abstract class Packet {
	private Object handle;

    public Object getHandle() {
        return handle;
    }

    private static Field playerConnectionField = makeField(getNmsClass("EntityPlayer"), "playerConnection");
    private static Method sendPacketMethod = makeMethod(getNmsClass("PlayerConnection"), "sendPacket", getNmsClass("Packet"));
	public void sendTo(Player p) {
		if (!isCompatible(p));
		Object handle = Reflection.getHandle(p);
		Object playerConnection = getField(playerConnectionField, handle);
		callMethod(sendPacketMethod, playerConnection, getHandle());
	}
	
	public abstract Class<?> getPacketClass();
	
	public boolean isCompatible(Player p) {
		return true;
	}

    // Utilities
    public static int toFixedPoint(double d) {
        return (int) Math.floor(d * 32.0D);
    }
    public static byte toByteAngle(double d) {
        return (byte) ((int) (d * 256.0F / 360.0F));
    }
    private static Field networkManagerField = makeField(getNmsClass("PlayerConnection"), "networkManager");
    private static Method getVersionMethod = makeMethod(getNmsClass("NetworkManager"), "getVersion");
    public static int getProtocolVersion(Player player) {
        Object handle = Reflection.getHandle(player);
        Object connection = getField(playerConnectionField, handle);
        Object networkManager = getField(networkManagerField, connection);
        if (getVersionMethod == null) { //Go to server's default version
            if (Reflection.getVersion().startsWith("v1_8")) {
                return 47;
            } else if (Reflection.getVersion().startsWith("v1_7")) {
                return 5;
            } else throw new RuntimeException("Unsupported server version " + Reflection.getVersion());
        }
        int version = callMethod(getVersionMethod, networkManager);
        return version;
    }
    public static int[] doVelocityMagic(double velocityX, double velocityY, double velocityZ) {
        double d0 = 3.9D;
        if (velocityX < -d0) {
            velocityX = -d0;
        }
        if (velocityY < -d0) {
            velocityY = -d0;
        }
        if (velocityZ < -d0) {
            velocityZ = -d0;
        }
        if (velocityX > d0) {
            velocityX = d0;
        }
        if (velocityY > d0) {
            velocityY = d0;
        }
        if (velocityZ > d0) {
            velocityZ = d0;
        }
        return new int[] {(int)(velocityX * 8000.0D), (int)(velocityY * 8000.0D), (int)(velocityZ * 8000.0D)}; //I wish methods could return multiple values
    }
}