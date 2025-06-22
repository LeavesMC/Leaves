package org.leavesmc.leaves.command;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DefaultQualifier(NonNull.class)
public class LeavesCommandUtil {

    private LeavesCommandUtil() {
    }

    // Code from Mojang - copyright them
    public static List<String> getListMatchingLast(
        final CommandSender sender,
        final String[] args,
        final String... matches
    ) {
        return getListMatchingLast(sender, args, Arrays.asList(matches));
    }

    public static boolean matches(final String s, final String s1) {
        return s1.regionMatches(true, 0, s, 0, s.length());
    }

    public static List<String> getListMatchingLast(
        final CommandSender sender,
        final String[] strings,
        final Collection<?> collection
    ) {
        return getListMatchingLast(sender, strings, collection, LeavesCommand.BASE_PERM, "bukkit.command.leaves");
    }

    public static List<String> getListMatchingLast(
        final CommandSender sender,
        final String[] strings,
        final Collection<?> collection,
        final String basePermission,
        final String overridePermission
    ) {
        String last = strings[strings.length - 1];
        ArrayList<String> results = Lists.newArrayList();

        if (!collection.isEmpty()) {

            for (String s1 : Iterables.transform(collection, Functions.toStringFunction())) {
                if (matches(last, s1) && (sender.hasPermission(basePermission + s1) || sender.hasPermission(overridePermission))) {
                    results.add(s1);
                }
            }

            if (results.isEmpty()) {
                for (Object object : collection) {
                    if (object instanceof ResourceLocation && matches(last, ((ResourceLocation) object).getPath())) {
                        results.add(String.valueOf(object));
                    }
                }
            }
        }

        return results;
    }
    // end copy stuff

    public static List<String> getListClosestMatchingLast(
        final CommandSender sender,
        final String last,
        final Collection<?> collection,
        final String overridePermission
    ) {
        ArrayList<Candidate> candidates = Lists.newArrayList();

        if (collection.isEmpty() || !sender.hasPermission(overridePermission)) {
            return Collections.emptyList();
        }

        String lastLower = last.toLowerCase();
        for (String item : Iterables.transform(collection, Functions.toStringFunction())) {
            String itemLower = item.toLowerCase();
            if (itemLower.startsWith(lastLower)) {
                candidates.add(Candidate.of(item, 0));
            } else if (itemLower.contains(lastLower)) {
                candidates.add(Candidate.of(item, damerauLevenshteinDistance(lastLower, itemLower)));
            }
        }
        candidates.sort(Comparator.comparingInt(c -> c.score));

        List<String> results = new ArrayList<>(candidates.size());
        for (Candidate candidate : candidates) {
            results.add(candidate.item);
        }

        return results;
    }

    /**
     * Computes the Dameraur-Levenshtein Distance between two strings. Adapted
     * from the algorithm at <a href="http://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance">Wikipedia: Damerau–Levenshtein distance</a>
     *
     * @param s1 The first string being compared.
     * @param s2 The second string being compared.
     * @return The number of substitutions, deletions, insertions, and
     * transpositions required to get from s1 to s2.
     */
    @SuppressWarnings("DuplicatedCode")
    private static int damerauLevenshteinDistance(@Nullable String s1, @Nullable String s2) {
        if (s1 == null && s2 == null) {
            return 0;
        }
        if (s1 != null && s2 == null) {
            return s1.length();
        }
        if (s1 == null && s2 != null) {
            return s2.length();
        }

        int s1Len = s1.length();
        int s2Len = s2.length();
        int[][] H = new int[s1Len + 2][s2Len + 2];

        int INF = s1Len + s2Len;
        H[0][0] = INF;
        for (int i = 0; i <= s1Len; i++) {
            H[i + 1][1] = i;
            H[i + 1][0] = INF;
        }
        for (int j = 0; j <= s2Len; j++) {
            H[1][j + 1] = j;
            H[0][j + 1] = INF;
        }

        Map<Character, Integer> sd = new HashMap<>();
        for (char Letter : (s1 + s2).toCharArray()) {
            if (!sd.containsKey(Letter)) {
                sd.put(Letter, 0);
            }
        }

        for (int i = 1; i <= s1Len; i++) {
            int DB = 0;
            for (int j = 1; j <= s2Len; j++) {
                int i1 = sd.get(s2.charAt(j - 1));
                int j1 = DB;

                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    H[i + 1][j + 1] = H[i][j];
                    DB = j;
                } else {
                    H[i + 1][j + 1] = Math.min(H[i][j], Math.min(H[i + 1][j], H[i][j + 1])) + 1;
                }

                H[i + 1][j + 1] = Math.min(H[i + 1][j + 1], H[i1][j1] + (i - i1 - 1) + 1 + (j - j1 - 1));
            }
            sd.put(s1.charAt(i - 1), i);
        }

        return H[s1Len + 1][s2Len + 1];
    }

    // Copy from org/bukkit/command/defaults/HelpCommand.java

    private record Candidate(String item, int score) {
        private static Candidate of(String item, int score) {
            return new Candidate(item, score);
        }
    }
}