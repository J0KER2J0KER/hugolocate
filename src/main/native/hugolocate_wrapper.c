/*
 * hugolocate_wrapper.c
 *
 * Thin wrapper around cubiomes so the Java/JNA side never needs to know
 * the layout of the opaque `Generator` struct. Only primitive types cross
 * the JNA boundary.
 */
#include "finders.h"

/*
 * Returns 1 if the given structure type is biome-viable at (x, z) for the
 * given Minecraft version, seed and dimension, 0 otherwise.
 *
 * mc:   Minecraft version constant (see cubiomes generator.h, e.g. MC_1_21)
 * structureType: cubiomes structure enum value (see finders.h, e.g. Shipwreck)
 * dim:  dimension constant (see cubiomes generator.h: DIM_OVERWORLD, DIM_NETHER, DIM_END)
 */
int hugolocate_is_viable(int mc, int structureType, long long seed, int x, int z, int dim) {
    Generator g;
    setupGenerator(&g, mc, 0);
    applySeed(&g, dim, (uint64_t) seed);
    return isViableStructurePos(structureType, &g, x, z, 0);
}