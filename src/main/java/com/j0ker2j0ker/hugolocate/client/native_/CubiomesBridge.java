package com.j0ker2j0ker.hugolocate.client.native_;

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
    private static final int MONUMENT = 8;
    private static final int VILLAGE = 5;
    private static final int RUINED_PORTAL = 11; // overworld variant, not Ruined_Portal_N
    private static final int FORTRESS = 18;
    private static final int BASTION = 19;

    // See cubiomes biomes.h enum Dimension
    private static final int DIM_NETHER = -1;
    private static final int DIM_OVERWORLD = 0;

    private interface CubiomesWrapper extends Library {
        CubiomesWrapper INSTANCE = Native.load("hugolocate_wrapper", CubiomesWrapper.class);

        int hugolocate_is_viable(int mc, int structureType, long seed, int x, int z, int dim);
    }

    public static boolean isViableShipwreckPos(long worldSeed, int x, int z) {
        return CubiomesWrapper.INSTANCE.hugolocate_is_viable(MC_1_21_WD, SHIPWRECK, worldSeed, x, z, DIM_OVERWORLD) != 0;
    }

    public static boolean isViableRuinedPortalPos(long worldSeed, int x, int z) {
        return CubiomesWrapper.INSTANCE.hugolocate_is_viable(MC_1_21_WD, RUINED_PORTAL, worldSeed, x, z, DIM_OVERWORLD) != 0;
    }

    public static boolean isViableMonumentPos(long worldSeed, int x, int z) {
        return CubiomesWrapper.INSTANCE.hugolocate_is_viable(MC_1_21_WD, MONUMENT, worldSeed, x, z, DIM_OVERWORLD) != 0;
    }

    public static boolean isViableVillagePos(long worldSeed, int x, int z) {
        return CubiomesWrapper.INSTANCE.hugolocate_is_viable(MC_1_21_WD, VILLAGE, worldSeed, x, z, DIM_OVERWORLD) != 0;
    }

    /** True if a Bastion generates here (Bastion has priority over Fortress when both are viable). */
    public static boolean isViableBastionPos(long worldSeed, int x, int z) {
        return CubiomesWrapper.INSTANCE.hugolocate_is_viable(MC_1_21_WD, BASTION, worldSeed, x, z, DIM_NETHER) != 0;
    }

    /** True if a Fortress generates here (i.e. Bastion is NOT viable at this position). */
    public static boolean isViableFortressPos(long worldSeed, int x, int z) {
        return !isViableBastionPos(worldSeed, x, z);
    }
}