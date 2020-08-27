// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.caves;

import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.nui.properties.Range;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generator.plugin.RegisterPlugin;

/**
 * Generates 2 independent noise functions, then puts caves where both of them are
 * close to 0. For topological reasons, this tends to produce caves in the shape
 * of continuous lines, which can't have dead ends, but may loop around and have
 * junctions (of an even number of tunnels).
 */
@RegisterPlugin
@Produces(CaveFacet.class)
@Requires({
    @Facet(SeaLevelFacet.class),
    @Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = 1))
})
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
        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);
        SurfaceHeightFacet surfaceHeight = region.getRegionFacet(SurfaceHeightFacet.class);

        CaveFacet facet = new CaveFacet(region.getRegion(), region.getBorderForFacet(CaveFacet.class));

        // get noise in batch for performance reasons.  Getting it by individual position takes 10 times as long
        float[][] caveNoiseValues = new float[][]{caveNoise[0].noise(facet.getWorldRegion()),caveNoise[1].noise(facet.getWorldRegion())};

        for (Vector3i pos : region.getRegion()) {
            float frequencyReduction = (float)Math.min(0.3,Math.max(0,(100+pos.y)/400.0)); //0: no reduction, 0.7: pretty much no caves. Also somewhat increases the tendency of caves to loop rather than continuing indefinitely.
            int i = facet.getWorldIndex(pos);
            float noiseValue = (float) Math.hypot(caveNoiseValues[0][i], caveNoiseValues[1][i]+frequencyReduction);
            
            boolean inCave = noiseValue < 0.08 - pos.y/1000f;
            boolean requiredSurface = pos.y <= seaLevel.getSeaLevel() && (
                   pos.y + 1 > surfaceHeight.getWorld(pos.x, pos.z)
                || pos.y + 1 > surfaceHeight.getWorld(pos.x + 1, pos.z)
                || pos.y + 1 > surfaceHeight.getWorld(pos.x - 1, pos.z)
                || pos.y + 1 > surfaceHeight.getWorld(pos.x, pos.z + 1)
                || pos.y + 1 > surfaceHeight.getWorld(pos.x, pos.z - 1)
            );
            facet.setWorld(pos, inCave && !requiredSurface);
        }

        region.setRegionFacet(CaveFacet.class, facet);
    }
}
