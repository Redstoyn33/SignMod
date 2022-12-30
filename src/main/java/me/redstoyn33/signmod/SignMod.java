package me.redstoyn33.signmod;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.ArrayUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Mod(modid = "signmod")
public class SignMod {
    public static String key = "";
    public static MessageDigest sha256;
    public static final int SIGN_SIZE = 44;
    public static final int MAX_LINE_TEXT = 375;

    static {
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] xor(byte[] msg, byte[] key) {
        byte[] out = new byte[msg.length];
        int j = 0;
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) (msg[i] ^ key[j++]);
            if (j == key.length) j = 0;
        }
        return out;
    }

    public static byte[] xor(byte[] msg, byte key) {
        byte[] out = new byte[msg.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) (msg[i] ^ key);
        }
        return out;
    }

    public static String byte2s(byte[] bytes) {
        boolean h = bytes.length % 2 == 0;
        int[] points = new int[h ? bytes.length / 2 + 1 : bytes.length / 2 + 2];
        for (int i = 0; i < points.length - 1; i++) {
            if (h) {
                points[i] = (Byte.toUnsignedInt(bytes[i * 2 + 1]) << 8) | Byte.toUnsignedInt(bytes[i * 2]);
            } else {
                if (i == points.length - 2) {
                    points[i] = Byte.toUnsignedInt(bytes[i * 2]);
                } else {
                    points[i] = (Byte.toUnsignedInt(bytes[i * 2 + 1]) << 8) | Byte.toUnsignedInt(bytes[i * 2]);
                }
            }
        }
        points[points.length - 1] = h ? 0 : 1;
        return new String(points, 0, points.length);
    }

    public static byte[] s2byte(String s) {
        int[] points = s.codePoints().toArray();
        int b = points[points.length - 1];
        if (!(b == 0) && !(b == 1)) return null;
        boolean h = b == 0;
        byte[] bytes = new byte[h ? (points.length - 1) * 2 : (points.length - 1) * 2 - 1];
        for (int i = 0; i < points.length - 1; i++) {
            bytes[i * 2] = uint2byte(points[i] & 0x00FF);
            if (h) {
                bytes[i * 2 + 1] = uint2byte((points[i] >> 8) & 0xFF);
            } else {
                if (i != points.length - 2) bytes[i * 2 + 1] = uint2byte((points[i] >> 8) & 0xFF);
            }
        }
        return bytes;
    }

    private static byte uint2byte(int i) {
        return (byte) (i > 127 ? i - 256 : i);
    }

    public static byte[] HMAC_SHA256(byte[] key,byte[] data){
        return sha256.digest(ArrayUtils.addAll(xor(key, (byte) 0x5C),sha256.digest(ArrayUtils.addAll(xor(key, (byte) 0x36),data))));
    }

    public static String pos2s(BlockPos pos){
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }
}
