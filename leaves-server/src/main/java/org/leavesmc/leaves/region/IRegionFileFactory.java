package org.leavesmc.leaves.region;

import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.region.linear.LinearRegionFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static net.minecraft.world.level.chunk.storage.RegionFileStorage.REGION_SHIFT;

public class IRegionFileFactory {

    private static final List<String> regionArguments;

    static {
        regionArguments = new ArrayList<>();
        for (RegionFileFormat format : RegionFileFormat.values()) {
            String arg = "." + format.getArgument();
            if (!regionArguments.contains(arg)) {
                regionArguments.add(arg);
            }
        }
    }

    public static void initFirstRegion(@NotNull RegionFileFormat firstFormat) {
        regionArguments.remove("." + firstFormat.getArgument());
        regionArguments.addFirst("." + firstFormat.getArgument());
    }

    @NotNull
    public static Pattern getRegionFileRegex() {
        String extensionsPattern = String.join("|", regionArguments.stream()
            .map(extension -> extension.replace(".", ""))
            .toList());
        return Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.(" + extensionsPattern + ")$");
    }

    @NotNull
    public static String getFirstRegionFileName(final int chunkX, final int chunkZ) {
        return "r." + (chunkX >> REGION_SHIFT) + "." + (chunkZ >> REGION_SHIFT) + regionArguments.getFirst();
    }

    @NotNull
    public static List<String> getRegionFileName(final int chunkX, final int chunkZ) {
        List<String> fileNames = new ArrayList<>();
        String regionName = "r." + (chunkX >> REGION_SHIFT) + "." + (chunkZ >> REGION_SHIFT);
        for (String argument : regionArguments) {
            fileNames.add(regionName + argument);
        }
        return fileNames;
    }

    public static boolean isRegionFile(String fileName) {
        for (String extension : regionArguments) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Contract("_, _, _, _ -> new")
    public static IRegionFile createRegionFile(RegionStorageInfo info, Path filePath, @NotNull Path folder, boolean sync) throws IOException {
        String extension = filePath.getFileName().toString().split("\\.")[3];

        switch (extension) {
            case "mca" -> {
                return new RegionFile(info, filePath, folder, sync);
            }
            case "linear" -> {
                return new LinearRegionFile(filePath, LeavesConfig.region.linear.version, LeavesConfig.region.linear.compressionLevel);
            }
        }

        throw new IOException("Unsupported region file format: " + extension);
    }
}
