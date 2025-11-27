package org.leavesmc.leaves.bot;

import com.mojang.logging.LogUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.util.TagUtil;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class BotDataStorage {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final File botDir;
    private final File botListFile;

    private CompoundTag savedBotList;

    public BotDataStorage(LevelStorageSource.@NotNull LevelStorageAccess session, String dataDir, String listFileName) {
        this.botDir = session.getLevelPath(new LevelResource(dataDir)).toFile();
        this.botListFile = session.getLevelPath(new LevelResource(listFileName)).toFile();
        this.botDir.mkdirs();

        this.savedBotList = new CompoundTag();
        if (this.botListFile.exists() && this.botListFile.isFile()) {
            try {
                Optional.of(NbtIo.readCompressed(this.botListFile.toPath(), NbtAccounter.unlimitedHeap())).ifPresent(tag -> this.savedBotList = tag);
            } catch (Exception exception) {
                BotDataStorage.LOGGER.warn("Failed to load player data list");
            }
        }
    }

    public void save(Player player) {
        boolean flag = true;
        try {
            CompoundTag nbt = TagUtil.saveEntityWithoutId(player);
            File file = new File(this.botDir, player.getStringUUID() + ".dat");

            if (file.exists() && file.isFile()) {
                if (!file.delete()) {
                    throw new IOException("Failed to delete file: " + file);
                }
            }
            if (!file.createNewFile()) {
                throw new IOException("Failed to create nbt file: " + file);
            }
            NbtIo.writeCompressed(nbt, file.toPath());
        } catch (Exception exception) {
            BotDataStorage.LOGGER.warn("Failed to save fakeplayer data for {}", player.getScoreboardName(), exception);
            flag = false;
        }

        if (flag && player instanceof ServerBot bot) {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("name", bot.createState.fullName());
            nbt.store("uuid", UUIDUtil.CODEC, bot.getUUID());
            nbt.putBoolean("resume", bot.resume);
            this.savedBotList.put(bot.createState.fullName(), nbt);
            this.saveBotList();
        }
    }


    public Optional<ValueInput> load(@NotNull ServerBot bot, ProblemReporter reporter) {
        return this.load(bot.nameAndId().name(), bot.nameAndId().id().toString()).map(nbt -> {
            ValueInput valueInput = TagValueInput.create(reporter, bot.registryAccess(), nbt);
            bot.load(valueInput);
            return valueInput;
        });
    }

    public void removeSavedData(@NotNull ServerBot bot) {
        this.load(bot.nameAndId().name(), bot.nameAndId().id().toString());
    }

    private Optional<CompoundTag> load(String name, String uuid) {
        File file = new File(this.botDir, uuid + ".dat");

        if (file.exists() && file.isFile()) {
            try {
                Optional<CompoundTag> optional = Optional.of(NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap()));
                if (!file.delete()) {
                    throw new IOException("Failed to delete fakeplayer data");
                }
                this.savedBotList.remove(name);
                this.saveBotList();
                return optional;
            } catch (Exception exception) {
                BotDataStorage.LOGGER.warn("Failed to load fakeplayer data for {}", name);
            }
        }

        return Optional.empty();
    }

    public Optional<CompoundTag> read(String uuid) {
        File file = new File(this.botDir, uuid + ".dat");
        if (file.exists() && file.isFile()) {
            try {
                return Optional.of(NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap()));
            } catch (Exception exception) {
                BotDataStorage.LOGGER.warn("Failed to read fakeplayer data for {}", uuid);
            }
        }
        return Optional.empty();
    }

    private void saveBotList() {
        try {
            if (this.botListFile.exists() && this.botListFile.isFile()) {
                if (!this.botListFile.delete()) {
                    throw new IOException("Failed to delete file: " + this.botListFile);
                }
            }
            if (!this.botListFile.createNewFile()) {
                throw new IOException("Failed to create nbt file: " + this.botListFile);
            }
            NbtIo.writeCompressed(this.savedBotList, this.botListFile.toPath());
        } catch (Exception exception) {
            BotDataStorage.LOGGER.warn("Failed to save player data list");
        }
    }

    public CompoundTag getSavedBotList() {
        return savedBotList;
    }
}
