package org.leavesmc.leaves.config;

public interface ConfigTransformer<FROM, TO> extends ConfigConverter<FROM> {
    TO transform(FROM from);
}
