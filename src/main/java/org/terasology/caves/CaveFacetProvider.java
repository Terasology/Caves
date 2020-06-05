/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.caves;

import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generator.plugin.RegisterPlugin;

/**
 * Generates 2 independent noise functions, then puts caves where both of them are
 * close to 0. For topological reasons, this tends to produce caves in the shape
 * of continuous lines, which can't have dead ends, but may loop around and have
 * junctions (of an even number of tunnels).
 */
@RegisterPlugin
@Produces(CaveFacet.class)
public class CaveFacetProvider implements FacetProviderPlugin {

    SubSampledNoise[] caveNoise = new SubSampledNoise[2];

    @Override
    public void setSeed(long seed) {
        for(int i=0; i<2; i++) {
            BrownianNoise baseNoise = new BrownianNoise(new SimplexNoise(seed + 2 + i), 4);
            caveNoise[i] = new SubSampledNoise(baseNoise, new Vector3f(0.006f, 0.006f, 0.006f), 4);
        }
    }

    @Override
    public void process(GeneratingRegion region) {

        CaveFacet facet = new CaveFacet(region.getRegion(), region.getBorderForFacet(CaveFacet.class));

        // get noise in batch for performance reasons.  Getting it by individual position takes 10 times as long
        float[][] caveNoiseValues = new float[][]{caveNoise[0].noise(facet.getWorldRegion()),caveNoise[1].noise(facet.getWorldRegion())};

        for (Vector3i pos : region.getRegion()) {
            float frequencyReduction = (float)Math.min(0.3,Math.max(0,(100+pos.y)/400.0)); //0: no reduction, 0.7: pretty much no caves. Also somewhat increases the tendency of caves to loop rather than continuing indefinitely.
            int i = facet.getWorldIndex(pos);
            float noiseValue = (float) Math.hypot(caveNoiseValues[0][i], caveNoiseValues[1][i]+frequencyReduction);

            facet.setWorld(pos, noiseValue < 0.08);
        }

        region.setRegionFacet(CaveFacet.class, facet);
    }
}
