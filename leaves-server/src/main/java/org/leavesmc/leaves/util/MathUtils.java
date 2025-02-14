package org.leavesmc.leaves.util;

import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class MathUtils {

    private static final Pattern numericPattern = Pattern.compile("^-?[1-9]\\d*$|^0$");

    public static boolean isNumeric(String str) {
        return numericPattern.matcher(str).matches();
    }

    public static float @NotNull [] fetchYawPitch(@NotNull Vector dir) {
        double x = dir.getX();
        double z = dir.getZ();

        float[] out = new float[2];

        if (x == 0.0D && z == 0.0D) {
            out[1] = (float) (dir.getY() > 0.0D ? -90 : 90);
        } else {
            double theta = Math.atan2(-x, z);
            out[0] = (float) Math.toDegrees((theta + 6.283185307179586D) % 6.283185307179586D);

            double x2 = NumberConversions.square(x);
            double z2 = NumberConversions.square(z);
            double xz = Math.sqrt(x2 + z2);
            out[1] = (float) Math.toDegrees(Math.atan(-dir.getY() / xz));
        }

        return out;
    }

    public static float fetchPitch(@NotNull Vector dir) {
        double x = dir.getX();
        double z = dir.getZ();

        float result;

        if (x == 0.0D && z == 0.0D) {
            result = (float) (dir.getY() > 0.0D ? -90 : 90);
        } else {
            double x2 = NumberConversions.square(x);
            double z2 = NumberConversions.square(z);
            double xz = Math.sqrt(x2 + z2);
            result = (float) Math.toDegrees(Math.atan(-dir.getY() / xz));
        }

        return result;
    }

    @NotNull
    public static Vector getDirection(double rotX, double rotY) {
        Vector vector = new Vector();

        rotX = Math.toRadians(rotX);
        rotY = Math.toRadians(rotY);

        double xz = Math.abs(Math.cos(rotY));

        vector.setX(-Math.sin(rotX) * xz);
        vector.setZ(Math.cos(rotX) * xz);
        vector.setY(-Math.sin(rotY));

        return vector;
    }

    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};

    public static int floorLog2(int value) {
        return ceilLog2(value) - (isPowerOfTwo(value) ? 0 : 1);
    }

    public static int ceilLog2(int value) {
        value = isPowerOfTwo(value) ? value : smallestEncompassingPowerOfTwo(value);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int) ((long) value * 125613361L >> 27) & 31];
    }

    public static boolean isPowerOfTwo(int value) {
        return value != 0 && (value & value - 1) == 0;
    }

    public static int smallestEncompassingPowerOfTwo(int value) {
        int i = value - 1;
        i |= i >> 1;
        i |= i >> 2;
        i |= i >> 4;
        i |= i >> 8;
        i |= i >> 16;
        return i + 1;
    }
}
