package org.leavesmc.leaves.protocol.chatimage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.leavesmc.leaves.LeavesLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ServerBlockCache {

    public static final ServerBlockCache SERVER_BLOCK_CACHE = new ServerBlockCache();

    public Cache<String, List<String>> userCache = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build();
    public Cache<String, Long> blockCacheTime = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build();
    public Cache<String, Map<Integer, String>> blockCache = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build();
    public Cache<String, Integer> fileCount = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build();

    public Map<Integer, String> createBlock(ChatImageIndex title, String imgBytes) {
        try {
            Map<Integer, String> blocks = this.blockCache.get(title.url, HashMap::new);
            if (blocks.isEmpty()) {
                this.blockCacheTime.put(title.url, System.currentTimeMillis());
            }
            blocks.put(title.index, imgBytes);
            this.blockCache.put(title.url, blocks);
            this.fileCount.put(title.url, title.total);
            return blocks;
        } catch (Exception e) {
            LeavesLogger.LOGGER.warning("Failed to create block for title " + title.url + ": " + e);
            return null;
        }
    }

    public Map<Integer, String> getBlock(String url) {
        Map<Integer, String> list;
        Integer total;
        if ((list = this.blockCache.getIfPresent(url)) != null && (total = this.fileCount.getIfPresent(url)) != null) {
            if (total == list.size()) {
                return list;
            }
        }
        return null;
    }

    public void tryAddUser(String url, String uuid) {
        try {
            List<String> names = this.userCache.get(url, Lists::newArrayList);
            names.add(uuid);
            this.userCache.put(url, names);
        } catch (Exception e) {
            LeavesLogger.LOGGER.warning("Failed to add user " + uuid + ": " + e);
        }
    }

    public List<String> getUsers(String url) {
        List<String> names;
        if ((names = this.userCache.getIfPresent(url)) != null) {
            this.userCache.put(url, Lists.newArrayList());
            return names;
        } else {
            return null;
        }
    }
}