package org.leavesmc.leaves.protocol.core;

import com.google.common.collect.ImmutableSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesLogger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LeavesProtocolManager {

    private static final Class<?>[] PAYLOAD_PARAMETER_TYPES = {ResourceLocation.class, FriendlyByteBuf.class};

    private static final LeavesLogger LOGGER = LeavesLogger.LOGGER;

    private static final Map<LeavesProtocol, Map<ProtocolHandler.PayloadReceiver, Executable>> KNOWN_TYPES = new HashMap<>();
    private static final Map<LeavesProtocol, Map<ProtocolHandler.PayloadReceiver, Method>> KNOW_RECEIVERS = new HashMap<>();
    private static Set<ResourceLocation> ALL_KNOWN_ID = new HashSet<>();

    private static final List<Method> TICKERS = new ArrayList<>();
    private static final List<Method> PLAYER_JOIN = new ArrayList<>();
    private static final List<Method> PLAYER_LEAVE = new ArrayList<>();
    private static final List<Method> RELOAD_SERVER = new ArrayList<>();
    private static final Map<LeavesProtocol, Map<ProtocolHandler.MinecraftRegister, Method>> MINECRAFT_REGISTER = new HashMap<>();

    public static void init() {
        for (Class<?> clazz : getClasses("org.leavesmc.leaves.protocol")) {
            final LeavesProtocol protocol = clazz.getAnnotation(LeavesProtocol.class);
            if (protocol != null) {
                Set<Method> methods;
                try {
                    Method[] publicMethods = clazz.getMethods();
                    Method[] privateMethods = clazz.getDeclaredMethods();
                    methods = new HashSet<>(publicMethods.length + privateMethods.length, 1.0f);
                    Collections.addAll(methods, publicMethods);
                    Collections.addAll(methods, privateMethods);
                } catch (NoClassDefFoundError error) {
                    LOGGER.severe("Failed to load class " + clazz.getName() + " due to missing dependencies, " + error.getCause() + ": " + error.getMessage());
                    return;
                }

                Map<ProtocolHandler.PayloadReceiver, Executable> map = KNOWN_TYPES.getOrDefault(protocol, new HashMap<>());
                for (final Method method : methods) {
                    if (method.isBridge() || method.isSynthetic() || !Modifier.isStatic(method.getModifiers())) {
                        continue;
                    }

                    method.setAccessible(true);

                    final ProtocolHandler.Init init = method.getAnnotation(ProtocolHandler.Init.class);
                    if (init != null) {
                        try {
                            method.invoke(null);
                        } catch (InvocationTargetException | IllegalAccessException exception) {
                            LOGGER.severe("Failed to invoke init method " + method.getName() + " in " + clazz.getName() + ", " + exception.getCause() + ": " + exception.getMessage());
                        }
                        continue;
                    }

                    final ProtocolHandler.PayloadReceiver receiver = method.getAnnotation(ProtocolHandler.PayloadReceiver.class);
                    if (receiver != null) {
                        try {
                            boolean found = false;
                            for (Method payloadMethod : receiver.payload().getDeclaredMethods()) {
                                if (payloadMethod.isAnnotationPresent(LeavesCustomPayload.New.class)) {
                                    if (Arrays.equals(payloadMethod.getParameterTypes(), PAYLOAD_PARAMETER_TYPES) && payloadMethod.getReturnType() == receiver.payload() && Modifier.isStatic(payloadMethod.getModifiers())) {
                                        payloadMethod.setAccessible(true);
                                        map.put(receiver, payloadMethod);
                                        found = true;
                                        break;
                                    }
                                }
                            }

                            if (!found) {
                                Constructor<? extends LeavesCustomPayload<?>> constructor = receiver.payload().getConstructor(PAYLOAD_PARAMETER_TYPES);
                                if (constructor.isAnnotationPresent(LeavesCustomPayload.New.class)) {
                                    constructor.setAccessible(true);
                                    map.put(receiver, constructor);
                                } else {
                                    throw new NoSuchMethodException();
                                }
                            }
                        } catch (NoSuchMethodException exception) {
                            LOGGER.severe("Failed to find constructor for " + receiver.payload().getName() + ", " + exception.getCause() + ": " + exception.getMessage());
                            continue;
                        }

                        if (!KNOW_RECEIVERS.containsKey(protocol)) {
                            KNOW_RECEIVERS.put(protocol, new HashMap<>());
                        }

                        KNOW_RECEIVERS.get(protocol).put(receiver, method);
                        continue;
                    }

                    final ProtocolHandler.Ticker ticker = method.getAnnotation(ProtocolHandler.Ticker.class);
                    if (ticker != null) {
                        TICKERS.add(method);
                        continue;
                    }

                    final ProtocolHandler.PlayerJoin playerJoin = method.getAnnotation(ProtocolHandler.PlayerJoin.class);
                    if (playerJoin != null) {
                        PLAYER_JOIN.add(method);
                        continue;
                    }

                    final ProtocolHandler.PlayerLeave playerLeave = method.getAnnotation(ProtocolHandler.PlayerLeave.class);
                    if (playerLeave != null) {
                        PLAYER_LEAVE.add(method);
                        continue;
                    }

                    final ProtocolHandler.ReloadServer reloadServer = method.getAnnotation(ProtocolHandler.ReloadServer.class);
                    if (reloadServer != null) {
                        RELOAD_SERVER.add(method);
                        continue;
                    }

                    final ProtocolHandler.MinecraftRegister minecraftRegister = method.getAnnotation(ProtocolHandler.MinecraftRegister.class);
                    if (minecraftRegister != null) {
                        if (!MINECRAFT_REGISTER.containsKey(protocol)) {
                            MINECRAFT_REGISTER.put(protocol, new HashMap<>());
                        }

                        MINECRAFT_REGISTER.get(protocol).put(minecraftRegister, method);
                    }
                }
                KNOWN_TYPES.put(protocol, map);
            }
        }

        for (LeavesProtocol protocol : KNOWN_TYPES.keySet()) {
            Map<ProtocolHandler.PayloadReceiver, Executable> map = KNOWN_TYPES.get(protocol);
            for (ProtocolHandler.PayloadReceiver receiver : map.keySet()) {
                if (receiver.sendFabricRegister() && !receiver.ignoreId()) {
                    for (String payloadId : receiver.payloadId()) {
                        for (String namespace : protocol.namespace()) {
                            ALL_KNOWN_ID.add(ResourceLocation.tryBuild(namespace, payloadId));
                        }
                    }
                }
            }
        }
        ALL_KNOWN_ID = ImmutableSet.copyOf(ALL_KNOWN_ID);
    }

    public static LeavesCustomPayload<?> decode(ResourceLocation id, FriendlyByteBuf buf) {
        for (LeavesProtocol protocol : KNOWN_TYPES.keySet()) {
            if (!ArrayUtils.contains(protocol.namespace(), id.getNamespace())) {
                continue;
            }

            Map<ProtocolHandler.PayloadReceiver, Executable> map = KNOWN_TYPES.get(protocol);
            for (ProtocolHandler.PayloadReceiver receiver : map.keySet()) {
                if (receiver.ignoreId() || ArrayUtils.contains(receiver.payloadId(), id.getPath())) {
                    try {
                        if (map.get(receiver) instanceof Constructor<?> constructor) {
                            return (LeavesCustomPayload<?>) constructor.newInstance(id, buf);
                        } else if (map.get(receiver) instanceof Method method) {
                            return (LeavesCustomPayload<?>) method.invoke(null, id, buf);
                        }
                    } catch (InvocationTargetException | InstantiationException | IllegalAccessException exception) {
                        LOGGER.warning("Failed to create payload for " + id + " in " + ArrayUtils.toString(protocol.namespace()) + ", " + exception.getCause() + ": " + exception.getMessage());
                        buf.readBytes(buf.readableBytes());
                        return new ErrorPayload(id, protocol.namespace(), receiver.payloadId());
                    }
                }
            }
        }
        return null;
    }

    public static void handlePayload(ServerPlayer player, LeavesCustomPayload<?> payload) {
        if (payload instanceof ErrorPayload errorPayload) {
            player.connection.disconnect(Component.literal("Payload " + Arrays.toString(errorPayload.packetID) + " from " + Arrays.toString(errorPayload.protocolID) + " error"), PlayerKickEvent.Cause.INVALID_PAYLOAD);
            return;
        }

        for (LeavesProtocol protocol : KNOW_RECEIVERS.keySet()) {
            if (!ArrayUtils.contains(protocol.namespace(), payload.type().id().getNamespace())) {
                continue;
            }

            Map<ProtocolHandler.PayloadReceiver, Method> map = KNOW_RECEIVERS.get(protocol);
            for (ProtocolHandler.PayloadReceiver receiver : map.keySet()) {
                if (payload.getClass() == receiver.payload()) {
                    if (receiver.ignoreId() || ArrayUtils.contains(receiver.payloadId(), payload.type().id().getPath())) {
                        try {
                            map.get(receiver).invoke(null, player, payload);
                        } catch (InvocationTargetException | IllegalAccessException exception) {
                            LOGGER.warning("Failed to handle payload " + payload.type().id() + " in " + ArrayUtils.toString(protocol.namespace()) + ", " + exception.getCause() + ": " + exception.getMessage());
                        }
                    }
                }
            }
        }
    }

    public static void handleTick() {
        if (!TICKERS.isEmpty()) {
            try {
                for (Method method : TICKERS) {
                    method.invoke(null);
                }
            } catch (InvocationTargetException | IllegalAccessException exception) {
                LOGGER.warning("Failed to tick, " + exception.getCause() + ": " + exception.getMessage());
            }
        }
    }

    public static void handlePlayerJoin(ServerPlayer player) {
        if (!PLAYER_JOIN.isEmpty()) {
            try {
                for (Method method : PLAYER_JOIN) {
                    method.invoke(null, player);
                }
            } catch (InvocationTargetException | IllegalAccessException exception) {
                LOGGER.warning("Failed to handle player join, " + exception.getCause() + ": " + exception.getMessage());
            }
        }

        ProtocolUtils.sendPayloadPacket(player, new FabricRegisterPayload(ALL_KNOWN_ID));
    }

    public static void handlePlayerLeave(ServerPlayer player) {
        if (!PLAYER_LEAVE.isEmpty()) {
            try {
                for (Method method : PLAYER_LEAVE) {
                    method.invoke(null, player);
                }
            } catch (InvocationTargetException | IllegalAccessException exception) {
                LOGGER.warning("Failed to handle player leave, " + exception.getCause() + ": " + exception.getMessage());
            }
        }
    }

    public static void handleServerReload() {
        if (!RELOAD_SERVER.isEmpty()) {
            try {
                for (Method method : RELOAD_SERVER) {
                    method.invoke(null);
                }
            } catch (InvocationTargetException | IllegalAccessException exception) {
                LOGGER.warning("Failed to handle server reload, " + exception.getCause() + ": " + exception.getMessage());
            }
        }
    }

    public static void handleMinecraftRegister(String channelId, ServerPlayer player) {
        for (LeavesProtocol protocol : MINECRAFT_REGISTER.keySet()) {
            String[] channel = channelId.split(":");
            if (!ArrayUtils.contains(protocol.namespace(), channel[0])) {
                continue;
            }

            Map<ProtocolHandler.MinecraftRegister, Method> map = MINECRAFT_REGISTER.get(protocol);
            for (ProtocolHandler.MinecraftRegister register : map.keySet()) {
                if (register.ignoreId() || ArrayUtils.contains(register.channelId(), channel[1])) {
                    try {
                        map.get(register).invoke(null, player, channel[1]);
                    } catch (InvocationTargetException | IllegalAccessException exception) {
                        LOGGER.warning("Failed to handle minecraft register, " + exception.getCause() + ": " + exception.getMessage());
                    }
                }
            }
        }
    }

    public static Set<Class<?>> getClasses(String pack) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        String packageDirName = pack.replace('.', '/');
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
                    findClassesInPackageByFile(pack, filePath, classes);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        Enumeration<JarEntry> entries = jar.entries();
                        findClassesInPackageByJar(pack, entries, packageDirName, classes);
                    } catch (IOException exception) {
                        LOGGER.warning("Failed to load jar file, " + exception.getCause() + ": " + exception.getMessage());
                    }
                }
            }
        } catch (IOException exception) {
            LOGGER.warning("Failed to load classes, " + exception.getCause() + ": " + exception.getMessage());
        }
        return classes;
    }

    private static void findClassesInPackageByFile(String packageName, String packagePath, Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirfiles = dir.listFiles((file) -> file.isDirectory() || file.getName().endsWith(".class"));
        if (dirfiles != null) {
            for (File file : dirfiles) {
                if (file.isDirectory()) {
                    findClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
                } else {
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    try {
                        classes.add(Class.forName(packageName + '.' + className));
                    } catch (ClassNotFoundException exception) {
                        LOGGER.warning("Failed to load class " + className + ", " + exception.getCause() + ": " + exception.getMessage());
                    }
                }
            }
        }
    }

    private static void findClassesInPackageByJar(String packageName, Enumeration<JarEntry> entries, String packageDirName, Set<Class<?>> classes) {
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }
            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf('/');
                if (idx != -1) {
                    packageName = name.substring(0, idx).replace('/', '.');
                }
                if (name.endsWith(".class") && !entry.isDirectory()) {
                    String className = name.substring(packageName.length() + 1, name.length() - 6);
                    try {
                        classes.add(Class.forName(packageName + '.' + className));
                    } catch (ClassNotFoundException exception) {
                        LOGGER.warning("Failed to load class " + className + ", " + exception.getCause() + ": " + exception.getMessage());
                    }
                }
            }
        }
    }

    public record ErrorPayload(ResourceLocation id, String[] protocolID, String[] packetID) implements LeavesCustomPayload<ErrorPayload> {
        @Override
        public void write(@NotNull FriendlyByteBuf buf) {
        }
    }

    public record EmptyPayload(ResourceLocation id) implements LeavesCustomPayload<EmptyPayload> {
        @New
        public EmptyPayload(ResourceLocation location, FriendlyByteBuf buf) {
            this(location);
        }

        @Override
        public void write(@NotNull FriendlyByteBuf buf) {
        }
    }

    public record LeavesPayload(FriendlyByteBuf data, ResourceLocation id) implements LeavesCustomPayload<LeavesPayload> {
        @New
        public LeavesPayload(ResourceLocation location, FriendlyByteBuf buf) {
            this(new FriendlyByteBuf(buf.readBytes(buf.readableBytes())), location);
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeBytes(data);
        }
    }

    public record FabricRegisterPayload(Set<ResourceLocation> channels) implements LeavesCustomPayload<FabricRegisterPayload> {

        public static final ResourceLocation CHANNEL = ResourceLocation.withDefaultNamespace("register");

        @New
        public FabricRegisterPayload(ResourceLocation location, FriendlyByteBuf buf) {
            this(buf.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            boolean first = true;

            ResourceLocation channel;
            for (Iterator<ResourceLocation> var3 = this.channels.iterator(); var3.hasNext(); buf.writeBytes(channel.toString().getBytes(StandardCharsets.US_ASCII))) {
                channel = var3.next();
                if (first) {
                    first = false;
                } else {
                    buf.writeByte(0);
                }
            }
        }

        @Override
        public ResourceLocation id() {
            return CHANNEL;
        }
    }
}
