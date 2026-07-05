package com.j0ker2j0ker.hugolocate.client;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * JNA binding to the native hugolocate_wrapper (which itself wraps cubiomes).
 * The native library must be named "hugolocate_wrapper" and placed under
 * JNA's platform-specific resource folders (win32-x86-64, darwin-x86-64,
 * darwin-aarch64, linux-x86-64) so it loads automatically on any platform.
 */
public class CubiomesBridge {

    // See cubiomes biomes.h enum MCVersion - no MC_1_21_4 constant exists;
    // MC_1_21_WD (index below) is the newest tracked version and is assumed
    // to also cover 1.21.4, since later patches did not change world gen.
    private static final int MC_1_21_WD = 28;

    // See cubiomes finders.h enum StructureType
    private static final int SHIPWRECK = 7;

    private interface CubiomesWrapper extends Library {
        CubiomesWrapper INSTANCE = Native.load("hugolocate_wrapper", CubiomesWrapper.class);

        int hugolocate_is_viable(int mc, int structureType, long seed, int x, int z);
    }

    public static boolean isViableShipwreckPos(long worldSeed, int x, int z) {
        return CubiomesWrapper.INSTANCE.hugolocate_is_viable(MC_1_21_WD, SHIPWRECK, worldSeed, x, z) != 0;
    }
}