/*
 * This file is part of LeafPile (https://github.com/Tuinity/LeafPile)
 *
 * LeafPile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LeafPile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LeafPile. If not, see <https://www.gnu.org/licenses/>.
 */

package ca.spottedleaf.common.util;

public final class ThrowUtil {

    private ThrowUtil() {}

    public static <T extends Throwable> void throwUnchecked(final Throwable thr) throws T {
        // noinspection unchecked
        throw (T)thr;
    }

    public static void closeAll(final AutoCloseable... closeables) {
        Throwable first = null;
        for (final AutoCloseable closeable : closeables) {
            if (closeable == null) {
                continue;
            }
            try {
                closeable.close();
            } catch (final Throwable thr) {
                if (first == null) {
                    first = thr;
                } else {
                    first.addSuppressed(thr);
                }
            }
        }
        if (first != null) {
            throwUnchecked(first);
        }
    }
}