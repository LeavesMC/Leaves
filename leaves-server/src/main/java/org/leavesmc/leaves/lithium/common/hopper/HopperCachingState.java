/*
 * This file is part of Lithium
 *
 * Lithium is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lithium is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Lithium. If not, see <https://www.gnu.org/licenses/>.
 */

package org.leavesmc.leaves.lithium.common.hopper;

public class HopperCachingState {

    public enum BlockInventory {
        UNKNOWN, // No information cached
        BLOCK_STATE, // Known to be Composter-like inventory (inventory from block, but not block entity, only depends on block state)
        BLOCK_ENTITY, // Known to be BlockEntity inventory without removal tracking capability
        REMOVAL_TRACKING_BLOCK_ENTITY, // Known to be BlockEntity inventory with removal tracking capability
        NO_BLOCK_INVENTORY // Known to be a block without hopper interaction (-> interact with entities instead)
    }
}