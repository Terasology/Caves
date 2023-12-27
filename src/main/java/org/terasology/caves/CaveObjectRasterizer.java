// Copyright 2019 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.caves;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.joml.Vector3ic;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.RequiresRasterizer;
import org.terasology.engine.world.generation.WorldRasterizerPlugin;

import java.util.List;
import java.util.Map;

/**
 * An example rasterizer for placing objects inside caves using {@link CaveLocationFacet}.
 */
//@RegisterPlugin /* uncomment to enable */
@RequiresRasterizer(CaveRasterizer.class)
public class CaveObjectRasterizer implements WorldRasterizerPlugin {

    private final Map<CaveObjectType, List<Block>> typeListEnumMap = Maps.newEnumMap(CaveObjectType.class);

    private BlockManager blockManager;

    @Override
    public void initialize() {
        blockManager = CoreRegistry.get(BlockManager.class);

        typeListEnumMap.put(CaveObjectType.DEFAULT, ImmutableList.<Block>of(
                blockManager.getBlockFamily("CoreAssets:Torch").getArchetypeBlock()));
    }

    @Override
    public void generateChunk(Chunk chunk, Region chunkRegion) {

        CaveObjectFacet facet = chunkRegion.getFacet(CaveObjectFacet.class);
        Block air = blockManager.getBlock(BlockManager.AIR_ID);

        Map<Vector3ic, CaveObjectType> entries = facet.getRelativeEntries();
        for (Vector3ic pos : entries.keySet()) {

            // check if some other rasterizer has already placed something here
            if (chunk.getBlock(pos).equals(air)) {

                CaveObjectType type = entries.get(pos);
                List<Block> list = typeListEnumMap.get(type);
                int blockIdx = 0;
                Block block = list.get(blockIdx);
                chunk.setBlock(pos, block);
            }
        }
    }
}
