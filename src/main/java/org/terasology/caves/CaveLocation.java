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

/**
 * A basic class providing the height of a cave's floor and ceiling.
 */
public class CaveLocation {
    /**
     * A value to indicate that the floor or ceiling was not found (either because no cave exists in this region,
     * or because the cave extends beyond this region, in which case the floor / ceiling will be provided in the
     * below / above region's CaveLocationFacet).
     */
    public static final float UNKNOWN = Float.NaN;

    public float ceiling = UNKNOWN;
    public float floor = UNKNOWN;
}
