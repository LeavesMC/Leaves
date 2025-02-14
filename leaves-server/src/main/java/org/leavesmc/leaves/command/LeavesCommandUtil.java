package org.leavesmc.leaves.command;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
            Iterator iterator = Iterables.transform(collection, Functions.toStringFunction()).iterator();

            while (iterator.hasNext()) {
                String s1 = (String) iterator.next();

                if (matches(last, s1) && (sender.hasPermission(basePermission + s1) || sender.hasPermission(overridePermission))) {
                    results.add(s1);
                }
            }

            if (results.isEmpty()) {
                iterator = collection.iterator();

                while (iterator.hasNext()) {
                    Object object = iterator.next();

                    if (object instanceof ResourceLocation && matches(last, ((ResourceLocation) object).getPath())) {
                        results.add(String.valueOf(object));
                    }
                }
            }
        }

        return results;
    }
    // end copy stuff
}