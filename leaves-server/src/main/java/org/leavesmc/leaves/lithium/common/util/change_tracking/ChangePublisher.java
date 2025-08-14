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

package org.leavesmc.leaves.lithium.common.util.change_tracking;

import net.minecraft.world.item.ItemStack;

public interface ChangePublisher<T> {
    void lithium$subscribe(ChangeSubscriber<T> subscriber, int subscriberData);

    int lithium$unsubscribe(ChangeSubscriber<T> subscriber);

    default void lithium$unsubscribeWithData(ChangeSubscriber<T> subscriber, int index) {
        throw new UnsupportedOperationException("Only implemented for ItemStacks");
    }

    default boolean lithium$isSubscribedWithData(ChangeSubscriber<ItemStack> subscriber, int subscriberData) {
        throw new UnsupportedOperationException("Only implemented for ItemStacks");
    }
}