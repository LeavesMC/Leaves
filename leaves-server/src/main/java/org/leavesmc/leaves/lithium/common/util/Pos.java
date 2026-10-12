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

package org.leavesmc.leaves.lithium.common.util;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelHeightAccessor;

public class Pos {

    public static class BlockCoord {
        public static int getYSize(LevelHeightAccessor view) {
            return view.getHeight();
        }
        public static int getMinY(LevelHeightAccessor view) {
            return view.getMinY();
        }
        public static int getMaxYInclusive(LevelHeightAccessor view) {
            return view.getMaxY();
        }
        public static int getMaxYExclusive(LevelHeightAccessor view) {
            return view.getMaxY() + 1;
        }

        public static int getMaxInSectionCoord(int sectionCoord) {
            return 15 + getMinInSectionCoord(sectionCoord);
        }

        public static int getMaxYInSectionIndex(LevelHeightAccessor view, int sectionIndex){
            return getMaxInSectionCoord(SectionYCoord.fromSectionIndex(view, sectionIndex));
        }

        public static int getMinInSectionCoord(int sectionCoord) {
            return SectionPos.sectionToBlockCoord(sectionCoord);
        }

        public static int getMinYInSectionIndex(LevelHeightAccessor view, int sectionIndex) {
            return getMinInSectionCoord(SectionYCoord.fromSectionIndex(view, sectionIndex));
        }
    }

    public static class ChunkCoord {
        public static int fromBlockCoord(int blockCoord) {
            return SectionPos.blockToSectionCoord(blockCoord);
        }

        public static int fromBlockSize(int i) {
            return i >> 4; //same method as fromBlockCoord, just be clear about coord/size semantic difference
        }
    }

    public static class SectionYCoord {
        public static int getNumYSections(LevelHeightAccessor view) {
            return view.getSectionsCount();
        }
        public static int getMinYSection(LevelHeightAccessor view) {
            return view.getMinSectionY();
        }
        public static int getMaxYSectionInclusive(LevelHeightAccessor view) {
            return view.getMaxSectionY();
        }
        public static int getMaxYSectionExclusive(LevelHeightAccessor view) {
            return view.getMaxSectionY() + 1;
        }

        public static int fromSectionIndex(LevelHeightAccessor view, int sectionCoord) {
            return sectionCoord + SectionYCoord.getMinYSection(view);
        }
        public static int fromBlockCoord(int blockCoord) {
            return SectionPos.blockToSectionCoord(blockCoord);
        }
    }

    public static class SectionYIndex {
        public static int getNumYSections(LevelHeightAccessor view) {
            return view.getSectionsCount();
        }
        public static int getMinYSectionIndex(LevelHeightAccessor view) {
            return 0;
        }
        public static int getMaxYSectionIndexInclusive(LevelHeightAccessor view) {
            return view.getSectionsCount() - 1;
        }
        public static int getMaxYSectionIndexExclusive(LevelHeightAccessor view) {
            return view.getSectionsCount();
        }


        public static int fromSectionCoord(LevelHeightAccessor view, int sectionCoord) {
            return sectionCoord - SectionYCoord.getMinYSection(view);
        }
        public static int fromBlockCoord(LevelHeightAccessor view, int blockCoord) {
            return fromSectionCoord(view, SectionPos.blockToSectionCoord(blockCoord));
        }
    }
}