/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.leavesmc.leaves.protocol.rei.transfer;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class RecipeFinder<T, I extends RecipeFinder.Ingredient<T>> {
    public final Reference2IntOpenHashMap<T> amounts = new Reference2IntOpenHashMap<>();

    public boolean contains(T item) {
        return this.amounts.getInt(item) > 0;
    }

    boolean containsAtLeast(T object, int i) {
        return this.amounts.getInt(object) >= i;
    }

    public void take(T item, int amount) {
        int taken = this.amounts.addTo(item, -amount);
        if (taken < amount) {
            throw new IllegalStateException("Took " + amount + " items, but only had " + taken);
        }
    }

    public void put(T item, int amount) {
        this.amounts.addTo(item, amount);
    }

    public boolean findRecipe(List<I> list, int maxCrafts, @Nullable BiConsumer<T, I> output) {
        return new Filter(list).tryPick(maxCrafts, output);
    }

    public int countRecipeCrafts(List<I> list, int maxCrafts, @Nullable BiConsumer<T, I> output) {
        return new Filter(list).tryPickAll(maxCrafts, output);
    }

    public void clear() {
        this.amounts.clear();
    }

    class Filter {
        private final List<I> ingredients;
        private final int ingredientCount;
        private final List<T> items;
        private final int itemCount;
        private final BitSet data;
        private final IntList path = new IntArrayList();

        public Filter(final List<I> list) {
            this.ingredients = list;
            this.ingredientCount = this.ingredients.size();
            this.items = this.getUniqueAvailableIngredientItems();
            this.itemCount = this.items.size();
            this.data = new BitSet(this.visitedIngredientCount() + this.visitedItemCount() + this.satisfiedCount() + this.connectionCount() + this.residualCount());
            this.setInitialConnections();
        }

        private void setInitialConnections() {
            for (int i = 0; i < this.ingredientCount; i++) {
                List<T> list = this.ingredients.get(i).elements();

                for (int j = 0; j < this.itemCount; j++) {
                    if (list.contains(this.items.get(j))) {
                        this.setConnection(j, i);
                    }
                }
            }
        }

        public boolean tryPick(int maxCrafts, @Nullable BiConsumer<T, I> output) {
            if (maxCrafts <= 0) {
                return true;
            } else {
                int j = 0;

                while (true) {
                    IntList intList = this.tryAssigningNewItem(maxCrafts);
                    if (intList == null) {
                        boolean bl = j == this.ingredientCount;
                        boolean bl2 = bl && output != null;
                        this.clearAllVisited();
                        this.clearSatisfied();

                        for (int l = 0; l < this.ingredientCount; l++) {
                            for (int m = 0; m < this.itemCount; m++) {
                                if (this.isAssigned(m, l)) {
                                    this.unassign(m, l);
                                    put(this.items.get(m), maxCrafts);
                                    if (bl2) {
                                        output.accept(this.items.get(m), this.ingredients.get(l));
                                    }
                                    break;
                                }
                            }
                        }

                        assert this.data.get(this.residualOffset(), this.residualOffset() + this.residualCount()).isEmpty();

                        return bl;
                    }

                    int k = intList.getInt(0);
                    take(this.items.get(k), maxCrafts);
                    int l = intList.size() - 1;
                    this.setSatisfied(intList.getInt(l));
                    j++;

                    for (int mx = 0; mx < intList.size() - 1; mx++) {
                        if (isPathIndexItem(mx)) {
                            int n = intList.getInt(mx);
                            int o = intList.getInt(mx + 1);
                            this.assign(n, o);
                        } else {
                            int n = intList.getInt(mx + 1);
                            int o = intList.getInt(mx);
                            this.unassign(n, o);
                        }
                    }
                }
            }
        }

        private static boolean isPathIndexItem(int i) {
            return (i & 1) == 0;
        }

        private List<T> getUniqueAvailableIngredientItems() {
            Set<T> set = new ReferenceOpenHashSet<>();

            for (Ingredient<T> ingredient : this.ingredients) {
                set.addAll(ingredient.elements());
            }

            set.removeIf(object -> !contains(object));
            return List.copyOf(set);
        }

        @Nullable
        private IntList tryAssigningNewItem(int i) {
            this.clearAllVisited();

            for (int j = 0; j < this.itemCount; j++) {
                if (containsAtLeast(this.items.get(j), i)) {
                    IntList intList = this.findNewItemAssignmentPath(j);
                    if (intList != null) {
                        return intList;
                    }
                }
            }

            return null;
        }

        @Nullable
        private IntList findNewItemAssignmentPath(int i) {
            this.path.clear();
            this.visitItem(i);
            this.path.add(i);

            while (!this.path.isEmpty()) {
                int j = this.path.size();
                int k = this.path.getInt(j - 1);
                if (isPathIndexItem(j - 1)) {
                    for (int l = 0; l < this.ingredientCount; l++) {
                        if (!this.hasVisitedIngredient(l) && this.hasConnection(k, l) && !this.isAssigned(k, l)) {
                            this.visitIngredient(l);
                            this.path.add(l);
                            break;
                        }
                    }
                } else {
                    if (!this.isSatisfied(k)) {
                        return this.path;
                    }

                    for (int lx = 0; lx < this.itemCount; lx++) {
                        if (!this.hasVisitedItem(lx) && this.isAssigned(lx, k)) {
                            assert this.hasConnection(lx, k);

                            this.visitItem(lx);
                            this.path.add(lx);
                            break;
                        }
                    }
                }

                int l = this.path.size();
                if (l == j) {
                    this.path.removeInt(l - 1);
                }
            }

            return null;
        }

        private int visitedIngredientOffset() {
            return 0;
        }

        private int visitedIngredientCount() {
            return this.ingredientCount;
        }

        private int visitedItemOffset() {
            return this.visitedIngredientOffset() + this.visitedIngredientCount();
        }

        private int visitedItemCount() {
            return this.itemCount;
        }

        private int satisfiedOffset() {
            return this.visitedItemOffset() + this.visitedItemCount();
        }

        private int satisfiedCount() {
            return this.ingredientCount;
        }

        private int connectionOffset() {
            return this.satisfiedOffset() + this.satisfiedCount();
        }

        private int connectionCount() {
            return this.ingredientCount * this.itemCount;
        }

        private int residualOffset() {
            return this.connectionOffset() + this.connectionCount();
        }

        private int residualCount() {
            return this.ingredientCount * this.itemCount;
        }

        private boolean isSatisfied(int i) {
            return this.data.get(this.getSatisfiedIndex(i));
        }

        private void setSatisfied(int i) {
            this.data.set(this.getSatisfiedIndex(i));
        }

        private int getSatisfiedIndex(int i) {
            assert i >= 0 && i < this.ingredientCount;

            return this.satisfiedOffset() + i;
        }

        private void clearSatisfied() {
            this.clearRange(this.satisfiedOffset(), this.satisfiedCount());
        }

        private void setConnection(int i, int j) {
            this.data.set(this.getConnectionIndex(i, j));
        }

        private boolean hasConnection(int i, int j) {
            return this.data.get(this.getConnectionIndex(i, j));
        }

        private int getConnectionIndex(int i, int j) {
            assert i >= 0 && i < this.itemCount;

            assert j >= 0 && j < this.ingredientCount;

            return this.connectionOffset() + i * this.ingredientCount + j;
        }

        private boolean isAssigned(int i, int j) {
            return this.data.get(this.getResidualIndex(i, j));
        }

        private void assign(int i, int j) {
            int k = this.getResidualIndex(i, j);

            assert !this.data.get(k);

            this.data.set(k);
        }

        private void unassign(int i, int j) {
            int k = this.getResidualIndex(i, j);

            assert this.data.get(k);

            this.data.clear(k);
        }

        private int getResidualIndex(int i, int j) {
            assert i >= 0 && i < this.itemCount;

            assert j >= 0 && j < this.ingredientCount;

            return this.residualOffset() + i * this.ingredientCount + j;
        }

        private void visitIngredient(int i) {
            this.data.set(this.getVisitedIngredientIndex(i));
        }

        private boolean hasVisitedIngredient(int i) {
            return this.data.get(this.getVisitedIngredientIndex(i));
        }

        private int getVisitedIngredientIndex(int i) {
            assert i >= 0 && i < this.ingredientCount;

            return this.visitedIngredientOffset() + i;
        }

        private void visitItem(int i) {
            this.data.set(this.getVisitiedItemIndex(i));
        }

        private boolean hasVisitedItem(int i) {
            return this.data.get(this.getVisitiedItemIndex(i));
        }

        private int getVisitiedItemIndex(int i) {
            assert i >= 0 && i < this.itemCount;

            return this.visitedItemOffset() + i;
        }

        private void clearAllVisited() {
            this.clearRange(this.visitedIngredientOffset(), this.visitedIngredientCount());
            this.clearRange(this.visitedItemOffset(), this.visitedItemCount());
        }

        private void clearRange(int i, int j) {
            this.data.clear(i, i + j);
        }

        public int tryPickAll(int i, @Nullable BiConsumer<T, I> output) {
            int j = 0;
            int k = Math.min(i, this.getMinIngredientCount()) + 1;

            while (true) {
                int l = (j + k) / 2;
                if (this.tryPick(l, null)) {
                    if (k - j <= 1) {
                        if (l > 0) {
                            this.tryPick(l, output);
                        }

                        return l;
                    }

                    j = l;
                } else {
                    k = l;
                }
            }
        }

        private int getMinIngredientCount() {
            int i = Integer.MAX_VALUE;

            for (Ingredient<T> ingredient : this.ingredients) {
                int j = 0;

                for (T object : ingredient.elements()) {
                    j = Math.max(j, amounts.getInt(object));
                }

                if (i > 0) {
                    i = Math.min(i, j);
                }
            }

            return i;
        }
    }

    public interface Ingredient<T> {
        List<T> elements();
    }
}