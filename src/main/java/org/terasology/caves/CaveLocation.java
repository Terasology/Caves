// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.caves;

/**
 * A basic class providing the height of a cave's floor and ceiling.
 */
public class CaveLocation {
    /**
     * A value to indicate that the floor or ceiling was not found (either because no cave exists in this region, or
     * because the cave extends beyond this region, in which case the floor / ceiling will be provided in the below /
     * above region's CaveLocationFacet).
     */
    public static final float UNKNOWN = Float.NaN;

    public float ceiling = UNKNOWN;
    public float floor = UNKNOWN;
}
