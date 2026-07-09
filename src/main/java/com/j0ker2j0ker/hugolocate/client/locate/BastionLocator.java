package com.j0ker2j0ker.hugolocate.client.locate;

import com.j0ker2j0ker.hugolocate.client.native_.CubiomesBridge;

/**
 * Predicts Bastion Remnant positions. Shares the same salt/placement
 * formula as Fortress (both use structure_set nether_complex in vanilla) -
 * distinguished only by which one is biome-viable at a given position.
 */
public class BastionLocator {

    private static final long WORLD_SEED = -2394875239847523984L;
    private static final long SALT = -163847291L;

    private static final int REGION_SIZE = 27; // chunks per region
    private static final int CHUNK_RANGE = 23; // spacing - separation = 27 - 4

    private static final long MASK_48 = (1L << 48) - 1;

    public static long[] predictChunk(long worldSeed, long salt, long regX, long regZ) {
        long seed = worldSeed + regX * 341873128712L + regZ * 132897987541L + salt;
        seed ^= 0x5deece66dL;
        seed = (seed * 0x5deece66dL + 0xbL) & MASK_48;
        long cx = extractBits(seed, CHUNK_RANGE);

        seed = (seed * 0x5deece66dL + 0xbL) & MASK_48;
        long cz = extractBits(seed, CHUNK_RANGE);

        long chunkX = regX * REGION_SIZE + cx;
        long chunkZ = regZ * REGION_SIZE + cz;
        return new long[]{chunkX, chunkZ};
    }

    private static long extractBits(long seed, int bound) {
        if ((bound & -bound) == bound) {
            return (bound * (seed >> 17)) >> 31;
        }
        return (seed >> 17) % bound;
    }

    private static long regionOfBlock(long coord) {
        return Math.floorDiv(coord, (long) REGION_SIZE * 16);
    }

    /** Up to 3 nearest Bastion positions, sorted by distance. */
    public static java.util.List<LocateResult> findNearest(long playerX, long playerZ, int searchRadiusRegions) {
        long playerRegX = regionOfBlock(playerX);
        long playerRegZ = regionOfBlock(playerZ);

        java.util.List<LocateResult> results = new java.util.ArrayList<>();

        for (int dx = -searchRadiusRegions; dx <= searchRadiusRegions; dx++) {
            for (int dz = -searchRadiusRegions; dz <= searchRadiusRegions; dz++) {
                long regX = playerRegX + dx;
                long regZ = playerRegZ + dz;
                long[] chunk = predictChunk(WORLD_SEED, SALT, regX, regZ);
                long blockX = chunk[0] * 16 + 9;
                long blockZ = chunk[1] * 16 + 9;

                if (!CubiomesBridge.isViableBastionPos(WORLD_SEED, (int) blockX, (int) blockZ)) {
                    continue;
                }

                double dist = Math.hypot(blockX - playerX, blockZ - playerZ);
                results.add(new LocateResult(blockX, blockZ, dist));
            }
        }

        results.sort(java.util.Comparator.comparingDouble(LocateResult::distance));
        return results.subList(0, Math.min(3, results.size()));
    }
}