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
import org.terasology.utilities.procedural.AbstractNoise;
import org.terasology.utilities.procedural.BrownianNoise3D;
import org.terasology.utilities.procedural.Noise3D;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.procedural.SubSampledNoise3D;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generator.plugin.RegisterPlugin;

@RegisterPlugin
@Produces(CaveFacet.class)
@Requires(@Facet(SurfaceHeightFacet.class))
public class CaveFacetProvider implements FacetProviderPlugin {
    private CaveFacetProviderConfiguration configuration = new CaveFacetProviderConfiguration();

    SubSampledNoise3D[] caveNoise = new SubSampledNoise3D[2];

    @Override
    public void setSeed(long seed) {
        for(int i=0; i<2; i++) {
            Noise3D pNoise = new SimplexNoise(seed + 2 + i);
            BrownianNoise3D bNoise = new BrownianNoise3D(pNoise, 3);
            bNoise.setPersistence(1.0);
            SubSampledNoise3D sNoise = new SubSampledNoise3D(bNoise, new Vector3f(0.01f, 0.01f, 0.01f), 4);
            caveNoise[i] = sNoise;
        }
    }

    @Override
    public void process(GeneratingRegion region) {

        CaveFacet facet = new CaveFacet(region.getRegion(), region.getBorderForFacet(CaveFacet.class));

        // get noise in batch for performance reasons.  Getting it by individual position takes 10 times as long
        float[][] caveNoiseValues = new float[][]{caveNoise[0].noise(facet.getWorldRegion()),caveNoise[1].noise(facet.getWorldRegion())};

        for (Vector3i pos : region.getRegion()) {
            int i = facet.getWorldIndex(pos);
            float noiseValue = (float) Math.hypot(caveNoiseValues[0][i], caveNoiseValues[1][i]);

            facet.setWorld(pos, noiseValue < 0.2);
        }

        region.setRegionFacet(CaveFacet.class, facet);
    }
}
