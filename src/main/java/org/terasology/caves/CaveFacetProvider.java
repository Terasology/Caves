// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.caves;

import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.generation.FacetProviderPlugin;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

/**
 * Generates 2 independent noise functions, then puts caves where both of them are close to 0. For topological reasons,
 * this tends to produce caves in the shape of continuous lines, which can't have dead ends, but may loop around and
 * have junctions (of an even number of tunnels).
 */
@RegisterPlugin
@Produces(CaveFacet.class)
public class CaveFacetProvider implements FacetProviderPlugin {

    SubSampledNoise[] caveNoise = new SubSampledNoise[2];

    @Override
    public void setSeed(long seed) {
        for (int i = 0; i < 2; i++) {
            BrownianNoise baseNoise = new BrownianNoise(new SimplexNoise(seed + 2 + i), 4);
            caveNoise[i] = new SubSampledNoise(baseNoise, new Vector3f(0.006f, 0.006f, 0.006f), 4);
        }
    }

    @Override
    public void process(GeneratingRegion region) {

        CaveFacet facet = new CaveFacet(region.getRegion(), region.getBorderForFacet(CaveFacet.class));

        // get noise in batch for performance reasons.  Getting it by individual position takes 10 times as long
        float[][] caveNoiseValues = new float[][]{caveNoise[0].noise(facet.getWorldRegion()),
                caveNoise[1].noise(facet.getWorldRegion())};

        for (Vector3i pos : region.getRegion()) {
            float frequencyReduction = (float) Math.min(0.3, Math.max(0, (100 + pos.y) / 400.0)); //0: no reduction, 
            // 0.7: pretty much no caves. Also somewhat increases the tendency of caves to loop rather than 
            // continuing indefinitely.
            int i = facet.getWorldIndex(pos);
            float noiseValue = (float) Math.hypot(caveNoiseValues[0][i], caveNoiseValues[1][i] + frequencyReduction);

            facet.setWorld(pos, noiseValue < 0.08);
        }

        region.setRegionFacet(CaveFacet.class, facet);
    }
}
