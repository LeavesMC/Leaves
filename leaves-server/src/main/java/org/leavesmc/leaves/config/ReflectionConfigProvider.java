package org.leavesmc.leaves.config;

import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.config.annotations.GlobalConfig;
import org.leavesmc.leaves.config.annotations.GlobalConfigCategory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ReflectionConfigProvider implements LeavesConfigProvider {

    public static final ReflectionConfigProvider INSTANCE = new ReflectionConfigProvider();
    private static final Object EMPTY = new Object();
    private static final Map<String, Node> CONFIG_NODES = new HashMap<>();

    public void init() {
        generateFlatMapNodes(LeavesConfig.class, null, "");
    }

    public LeavesConfigValue getConfig(String configNode) {
        return CONFIG_NODES.get(configNode).get();
    }

    public void setConfig(String configNode, LeavesConfigValue configValue) {
        CONFIG_NODES.get(configNode).set(configValue);
    }

    private void generateFlatMapNodes(Class<?> clazz, Object instance, String parent) {
        GlobalConfigCategory category = clazz.getAnnotation(GlobalConfigCategory.class);
        Map<Class<?>, Object> innerClasses = new HashMap<>();
        for (var inner : clazz.getDeclaredClasses()) {
            innerClasses.put(inner, EMPTY);
        }
        for (var field : clazz.getDeclaredFields()) {
            if (innerClasses.containsKey(field.getType())) {
                try {
                    innerClasses.put(field.getType(), field.get(instance));
                } catch (Exception e) {
                    throw new RuntimeException();
                }
            }
            GlobalConfig globalConfig = field.getAnnotation(GlobalConfig.class);
            if (globalConfig != null) {
                String nodeName = parent + "." + globalConfig.value();
                CONFIG_NODES.put(nodeName, new Node(instance, field, field.getType()));
            }
        }
        String nodeName = (category == null) ? parent : parent + "." + category.value();
        for (var entry : innerClasses.entrySet()) {
            if (entry.getValue().equals(EMPTY)) {
                throw new IllegalStateException("Inner class is not accessible: " + entry.getKey());
            }
            generateFlatMapNodes(entry.getKey(), entry.getValue(), nodeName);
        }
    }

    private record Node(Object instance, Field accessor, Class<?> type) {

        LeavesConfigValue get() {
            try {
                return new LeavesConfigValue(accessor.get(instance));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("all")
        void set(LeavesConfigValue configValue) {
            Object value;
            if (type.isEnum()) {
                value = Enum.valueOf((Class<? extends Enum>) type, configValue.getString());
            } else {
                value = switch (accessor.getType().getSimpleName()) {
                    case "Integer" -> configValue.getInt();
                    case "String" -> configValue.getString();
                    case "Boolean" -> configValue.getBoolean();
                    case "Double" -> configValue.getDouble();
                    default -> throw new IllegalStateException("This couldn't happen");
                };
            }
            try {
                accessor.set(instance, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
