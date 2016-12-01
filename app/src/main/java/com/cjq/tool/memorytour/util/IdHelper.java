package com.cjq.tool.memorytour.util;

import android.util.Base64;

import java.util.UUID;

/**
 * Created by KAT on 2016/9/20.
 */
public class IdHelper {

    private IdHelper() {
    }

    public static String compressedUuid() {
        UUID uuid = UUID.randomUUID();
        return compressUuid(uuid);
    }

    public static String compressUuid(UUID uuid) {
        byte[] byUuid = new byte[16];
        long least = uuid.getLeastSignificantBits();
        long most = uuid.getMostSignificantBits();
        long2Bytes(most, byUuid, 0);
        long2Bytes(least, byUuid, 8);
        //String compressUUID = Base64.encodeBase64URLSafeString(byUuid);
        String compressUUID = Base64.encodeToString(byUuid, Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP);
        return compressUUID;
    }

    private static void long2Bytes(long value, byte[] bytes, int offset) {
        for (int i = 7; i > -1; i--) {
            bytes[offset++] = (byte) ((value >> 8 * i) & 0xFF);
        }
    }

    public static String compressUuid(String uuidString) {
        UUID uuid = UUID.fromString(uuidString);
        return compressUuid(uuid);
    }

    public static String unzipUuid(String compressedUuid) {
        if (compressedUuid.length() != 22) {
            throw new IllegalArgumentException("Invalid uuid!");
        }
        //byte[] byUuid = Base64.decodeBase64(compressedUuid + "==");
        byte[] byUuid = Base64.decode(compressedUuid + "==", Base64.URL_SAFE);
        long most = bytes2Long(byUuid, 0);
        long least = bytes2Long(byUuid, 8);
        UUID uuid = new UUID(most, least);
        return uuid.toString();
    }

    private static long bytes2Long(byte[] bytes, int offset) {
        long value = 0;
        for (int i = 7; i > -1; i--) {
            value |= (((long) bytes[offset++]) & 0xFF) << 8 * i;
        }
        return value;
    }
}
