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

package org.leavesmc.leaves.lithium.api.inventory;

public interface LithiumCooldownReceivingInventory {

    /**
     * To be implemented by modded inventories that want to receive hopper-like transfer cooldowns from lithium's (!)
     * item transfers. Hopper-like transfer cooldown means a cooldown that is only set if the hopper was empty before
     * the transfer.
     * NOTE: Lithium does not replace all of vanilla's item transfers. Mod authors still need to implement
     * their own hooks for vanilla code even when they require users to install Lithium.
     *
     * @param currentTime tick time of the item transfer.
     */
    default void setTransferCooldown(long currentTime) {
    }


    /**
     * To be implemented by modded inventories that want to receive hopper-like transfer cooldowns from lithium's (!)
     * item transfers. Hopper-like transfer cooldown means a cooldown that is only set if the hopper was empty before
     * the transfer.
     * NOTE: Lithium does not replace all of vanilla's item transfers. Mod authors still need to implement
     * their own hooks for vanilla code even when they require users to install Lithium.
     *
     * @return Whether this inventory wants to receive transfer cooldowns from lithium's code
     */
    default boolean canReceiveTransferCooldown() {
        return false;
    }
}