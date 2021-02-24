// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.caves;

import org.joml.Math;
import org.joml.Vector3ic;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.ElevationFacet;
import org.terasology.world.generator.plugin.RegisterPlugin;

/**
 * Generates 2 independent noise functions, then puts caves where both of them are
 * close to 0. For topological reasons, this tends to produce caves in the shape
 * of continuous lines, which can't have dead ends, but may loop around and have
 * junctions (of an even number of tunnels).
 */
@RegisterPlugin
@Produces(CaveFacet.class)
@Requires(@Facet(ElevationFacet.class))
public class CaveFacetProvider implements FacetProviderPlugin {

    SubSampledNoise[] caveNoise = new SubSampledNoise[2];

    @Override
    public void setSeed(long seed) {
        for(int i=0; i<2; i++) {
            BrownianNoise baseNoise = new BrownianNoise(new SimplexNoise(seed + 2 + i), 4);
            caveNoise[i] = new SubSampledNoise(baseNoise, new org.joml.Vector3f(0.006f, 0.006f, 0.006f), 4);
        }
    }

    @Override
    public void process(GeneratingRegion region) {
        ElevationFacet elevationFacet = region.getRegionFacet(ElevationFacet.class);
        CaveFacet facet = new CaveFacet(region.getRegion(), region.getBorderForFacet(CaveFacet.class));

        // get noise in batch for performance reasons.  Getting it by individual position takes 10 times as long
        float[][] caveNoiseValues = new float[][]{caveNoise[0].noise(facet.getWorldRegion()), caveNoise[1].noise(facet.getWorldRegion())};

        for (Vector3ic pos : facet.getWorldRegion()) {
            float depth = elevationFacet.getWorld(pos.x(), pos.z()) - pos.y();
            float frequencyReduction = (float) Math.max(0, 0.3 - Math.max(depth, 0) / 400); //0: no reduction, 0.7: pretty much no caves. Also somewhat increases the tendency of caves to loop rather than continuing indefinitely.
            int i = facet.getWorldIndex(pos);
            float xx = caveNoiseValues[0][i];
            float yy = caveNoiseValues[1][i] + frequencyReduction;
            float freqDepth = 0.06f + depth / 2000f;
            facet.setWorld(pos, (xx * xx + yy * yy) < freqDepth * freqDepth);
        }

        region.setRegionFacet(CaveFacet.class, facet);
    }
}
