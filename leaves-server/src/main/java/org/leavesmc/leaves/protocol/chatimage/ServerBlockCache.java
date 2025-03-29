package org.leavesmc.leaves.protocol.chatimage;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerBlockCache {

    public static final ServerBlockCache SERVER_BLOCK_CACHE = new ServerBlockCache();

    public Map<String, List<String>> userCache = new HashMap<>();
    public Map<String, Long> blockCacheTime = new HashMap<>();
    public Map<String, Map<Integer, String>> blockCache = new HashMap<>();
    public Map<String, Integer> fileCount = new HashMap<>();

    public Map<Integer, String> createBlock(ChatImageIndex title, String imgBytes) {
        Map<Integer, String> blocks = this.blockCache.containsKey(title.url) ? this.blockCache.get(title.url) : new HashMap<>();
        if (blocks.isEmpty()) {
            this.blockCacheTime.put(title.url, System.currentTimeMillis());
        }
        blocks.put(title.index, imgBytes);
        this.blockCache.put(title.url, blocks);
        this.fileCount.put(title.url, title.total);
        return blocks;
    }

    public Map<Integer, String> getBlock(String url) {
        if (this.blockCache.containsKey(url) && this.fileCount.containsKey(url)) {
            Map<Integer, String> list = this.blockCache.get(url);
            Integer total = this.fileCount.get(url);
            if (total == list.size()) {
                return list;
            }
        }

        return null;
    }

    public String getImage(String url) {
        Map<Integer, String> blocks = this.getBlock(url);
        StringBuilder base64Img = new StringBuilder();
        if (blocks == null) {
            return null;
        } else {
            for (int i = 1; i <= blocks.size(); ++i) {
                base64Img.append(ChatImageProtocol.gson.fromJson((String) blocks.get(i), ChatImageIndex.class).bytes);
            }

            return base64Img.toString();
        }
    }

    public void tryAddUser(String url, String uuid) {
        List<String> names = this.userCache.containsKey(url) ? this.userCache.get(url) : Lists.newArrayList();
        names.add(uuid);
        this.userCache.put(url, names);
    }

    public List<String> getUsers(String url) {
        if (this.userCache.containsKey(url)) {
            List<String> names = this.userCache.get(url);
            this.userCache.put(url, Lists.newArrayList());
            return names;
        } else {
            return null;
        }
    }

    public void clear(long timestamp) {
        List<String> keys = Lists.newArrayList();
        for (Map.Entry<String, Long> entry : this.blockCacheTime.entrySet()) {
            if (entry.getValue() < timestamp) {
                keys.add(entry.getKey());
            }
        }
        for (String key : keys) {
            this.blockCache.remove(key);
            this.blockCacheTime.remove(key);
            this.fileCount.remove(key);
            this.userCache.remove(key);
        }
    }

    public void clear() {
        this.userCache.clear();
        this.blockCache.clear();
        this.blockCacheTime.clear();
        this.fileCount.clear();
    }
}