package org.leavesmc.leaves.bot.agent;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public record ExtraData(List<Pair<String, String>> raw) {
    public void add(String key, String value) {
        raw.add(Pair.of(key, value));
    }
}
