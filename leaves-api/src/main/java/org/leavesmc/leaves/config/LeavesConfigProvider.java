package org.leavesmc.leaves.config;

public interface LeavesConfigProvider {

    LeavesConfigValue getConfig(String configNode);

    void setConfig(String configNode, LeavesConfigValue value);
}
