// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.caves;

import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.Updates;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfacesFacet;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.HashSet;
import java.util.Set;

/**
 * Removes the surfaces where they're broken by caves, moving them to the exposed
 * cave floor instead. Also remove the caves above the surface and breaking the
 * sea-floor.
 */
@RegisterPlugin
@Updates({
    @Facet(value = SurfacesFacet.class, border = @FacetBorder(sides = CaveToSurfaceProvider.SURFACE_SPREAD, bottom = CaveToSurfaceProvider.SURFACE_SPREAD, top = 20)),
    @Facet(value = CaveFacet.class, border = @FacetBorder(sides = CaveToSurfaceProvider.SURFACE_SPREAD, bottom = CaveToSurfaceProvider.SURFACE_SPREAD, top = 20))
})
@Requires({
    @Facet(SeaLevelFacet.class),
    @Facet(value = DensityFacet.class, border = @FacetBorder(sides = CaveToSurfaceProvider.SURFACE_SPREAD, bottom = CaveToSurfaceProvider.SURFACE_SPREAD, top = 20))
})
public class CaveToSurfaceProvider implements FacetProviderPlugin {
    public static final int SURFACE_SPREAD = 3;

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {
        CaveFacet caveFacet = region.getRegionFacet(CaveFacet.class);
        DensityFacet densityFacet = region.getRegionFacet(DensityFacet.class);
        SurfacesFacet surfacesFacet = region.getRegionFacet(SurfacesFacet.class);
        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);

        Set<Vector3i> cavePositions = new HashSet<Vector3i>();

        for (Vector3i pos : caveFacet.getWorldRegion()) {
            if (caveFacet.getWorld(pos) && densityFacet.getWorldRegion().encompasses(pos) && surfacesFacet.getWorldRegion().encompasses(pos)) {
                cavePositions.add(pos);
            }
        }

        // Ensure that the ocean can't immediately fall into a cave.
        for (Vector3i pos : densityFacet.getWorldRegion()) {
            if (densityFacet.getWorld(pos) <= 0) {
                if (cavePositions.contains(pos)) {
                    cavePositions.remove(pos);
                    caveFacet.setWorld(pos, false);
                }
                if (pos.y <= seaLevel.getSeaLevel() + 1) {
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            Vector3i belowPos = new Vector3i(pos.x + x, pos.y - 1, pos.z + z);
                            if (cavePositions.contains(belowPos)) {
                                cavePositions.remove(belowPos);
                                caveFacet.setWorld(belowPos, false);
                            }
                        }
                    }
                }
            }
        }

        // Mark any cave floors exposed to the sky as surface.
        Set<Vector3i> newSurfaces = new HashSet<>();
        for (Vector3i pos : cavePositions) {
            if (surfacesFacet.getWorld(JomlUtil.from(pos))) {
                surfacesFacet.setWorld(JomlUtil.from(pos), false);
                Vector3i newSurface = new Vector3i(pos);
                while (cavePositions.contains(newSurface)) {
                    newSurface.addY(-1);
                }
                if (newSurface.y >= surfacesFacet.getWorldRegion().minY()) {
                    newSurfaces.add(newSurface);
                    surfacesFacet.setWorld(JomlUtil.from(newSurface), true);
                }
            }
        }

        // Mark cave floors near to those exposed to the sky as surface.
        for (int i = 0; i < SURFACE_SPREAD; i++) {
            Set<Vector3i> newerSurfaces = new HashSet<>();
            for (Vector3i surface : newSurfaces) {
                for (Vector3i adjacent : new Vector3i[]{
                    new Vector3i(surface).subX(1),
                    new Vector3i(surface).addX(1),
                    new Vector3i(surface).subZ(1),
                    new Vector3i(surface).addZ(1)
                }) {
                    while (!cavePositions.contains(adjacent) && densityFacet.getWorldRegion().encompasses(adjacent) && densityFacet.getWorld(adjacent) > 0) {
                        adjacent.addY(1);
                    }
                    // Only continue if the selected position is actually in a cave, rather than on the surface or above the selected region.
                    if (cavePositions.contains(adjacent)) {
                        while (cavePositions.contains(adjacent)) {
                            adjacent.subY(1);
                        }
                        if (
                            surfacesFacet.getWorldRegion().encompasses(adjacent) &&
                            densityFacet.getWorldRegion().encompasses(adjacent) &&
                            densityFacet.getWorld(adjacent) > 0 &&
                            !surfacesFacet.getWorld(JomlUtil.from(adjacent))
                        ) {
                            newerSurfaces.add(adjacent);
                            surfacesFacet.setWorld(JomlUtil.from(adjacent), true);
                        }
                    }
                }
            }
            newSurfaces = newerSurfaces;
        }
    }
}
