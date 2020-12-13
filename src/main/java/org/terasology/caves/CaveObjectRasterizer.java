/*
 * Copyright 2019 MovingBlocks
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.joml.Vector3ic;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.RequiresRasterizer;
import org.terasology.world.generation.WorldRasterizerPlugin;
import org.terasology.world.generator.plugin.RegisterPlugin;

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
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {

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
