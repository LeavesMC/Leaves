package org.leavesmc.leaves.config;

public class InternalConfigProvider implements LeavesConfigProvider {

    public static final InternalConfigProvider INSTANCE = new InternalConfigProvider();

    public LeavesConfigValue getConfig(String configNode) {
        return new LeavesConfigValue(GlobalConfigManager.getVerifiedConfig(configNode).get());
    }

    public void setConfig(String configNode, LeavesConfigValue configValue) {
        GlobalConfigManager.getVerifiedConfig(configNode).set(configValue.toString());
    }
}
