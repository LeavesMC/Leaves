package org.leavesmc.leaves.bot;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class BotDataStorage implements IPlayerDataStorage {

    private static final LevelResource BOT_DATA_DIR = new LevelResource("fakeplayerdata");
    private static final LevelResource BOT_LIST_FILE = new LevelResource("fakeplayer.dat");

    private static final Logger LOGGER = LogUtils.getLogger();
    private final File botDir;
    private final File botListFile;

    private CompoundTag savedBotList;

    public BotDataStorage(LevelStorageSource.@NotNull LevelStorageAccess session) {
        this.botDir = session.getLevelPath(BOT_DATA_DIR).toFile();
        this.botListFile = session.getLevelPath(BOT_LIST_FILE).toFile();
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

    @Override
    public void save(Player player) {
        boolean flag = true;
        try {
            CompoundTag nbt = player.saveWithoutId(new CompoundTag());
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
            nbt.putString("name", bot.createState.name());
            nbt.putUUID("uuid", bot.getUUID());
            nbt.putBoolean("resume", bot.resume);
            this.savedBotList.put(bot.createState.realName(), nbt);
            this.saveBotList();
        }
    }

    @Override
    public Optional<CompoundTag> load(Player player) {
        return this.load(player.getScoreboardName(), player.getStringUUID()).map((nbt) -> {
            player.load(nbt);
            return nbt;
        });
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
