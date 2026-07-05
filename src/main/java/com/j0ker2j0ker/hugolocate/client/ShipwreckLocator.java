package com.j0ker2j0ker.hugolocate.client;

/**
 * Predicts shipwreck generation-attempt chunk positions from the region
 * seed formula. Does not check biome viability - that comes later via
 * a native cubiomes binding.
 */
public class ShipwreckLocator {

    private static final long WORLD_SEED = -2394875239847523984L;
    private static final long SALT = 2048571934L;

    private static final int REGION_SIZE = 24; // chunks per region
    private static final int CHUNK_RANGE = 20; // spacing - separation = 24 - 4

    private static final long MASK_48 = (1L << 48) - 1;

    /** Returns {chunkX, chunkZ} of the shipwreck attempt position for the given region. */
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
        if ((bound & -bound) == bound) { // power of 2
            return (bound * (seed >> 17)) >> 31;
        }
        return (seed >> 17) % bound;
    }

    private static long regionOfBlock(long coord) {
        return Math.floorDiv(coord, (long) REGION_SIZE * 16);
    }

    /** Block position (with +9 attempt offset) of the nearest predicted shipwreck. */
    public static long[] findNearest(long playerX, long playerZ, int searchRadiusRegions) {
        long playerRegX = regionOfBlock(playerX);
        long playerRegZ = regionOfBlock(playerZ);

        long[] best = null;
        double bestDist = Double.MAX_VALUE;

        for (int dx = -searchRadiusRegions; dx <= searchRadiusRegions; dx++) {
            for (int dz = -searchRadiusRegions; dz <= searchRadiusRegions; dz++) {
                long regX = playerRegX + dx;
                long regZ = playerRegZ + dz;
                long[] chunk = predictChunk(WORLD_SEED, SALT, regX, regZ);
                long blockX = chunk[0] * 16 + 9;
                long blockZ = chunk[1] * 16 + 9;

                double dist = Math.hypot(blockX - playerX, blockZ - playerZ);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = new long[]{blockX, blockZ};
                }
            }
        }
        return best;
    }
}