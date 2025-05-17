package org.leavesmc.leaves.protocol.core;

import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.player.PlayerKickEvent;
import org.leavesmc.leaves.LeavesLogger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

// TODO refactor
public class LeavesProtocolManager {

    private static final LeavesLogger LOGGER = LeavesLogger.LOGGER;

    private static final Map<String, InvokerHolder<ProtocolHandler.PayloadReceiver>> PAYLOAD_RECEIVERS = new HashMap<>();
    private static final Map<String, InvokerHolder<ProtocolHandler.BytebufReceiver>> BYTEBUF_RECEIVERS = new HashMap<>();
    private static final Map<String, InvokerHolder<ProtocolHandler.MinecraftRegister>> KEYED_MINECRAFT_REGISTER = new HashMap<>();

    private static final List<InvokerHolder<ProtocolHandler.Ticker>> TICKERS = new ArrayList<>();
    private static final List<InvokerHolder<ProtocolHandler.PlayerJoin>> PLAYER_JOIN = new ArrayList<>();
    private static final List<InvokerHolder<ProtocolHandler.PlayerLeave>> PLAYER_LEAVE = new ArrayList<>();
    private static final List<InvokerHolder<ProtocolHandler.ReloadServer>> RELOAD_SERVER = new ArrayList<>();
    private static final List<InvokerHolder<ProtocolHandler.MinecraftRegister>> WILD_MINECRAFT_REGISTER = new ArrayList<>();

    private static Set<String> ALL_KNOWN_ID = new HashSet<>();

    private static final Map<ResourceLocation, StreamCodec<FriendlyByteBuf, LeavesCustomPayload<?>>> DECODERS = new HashMap<>();
    private static final Map<Class<?>, StreamCodec<FriendlyByteBuf, LeavesCustomPayload<?>>> ENCODERS = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static void init() {
        for (Class<?> clazz : getClasses("org.leavesmc.leaves.protocol")) {
            if (clazz.isAssignableFrom(LeavesCustomPayload.class)) {
                ResourceLocation location;
                StreamCodec<FriendlyByteBuf, LeavesCustomPayload<?>> streamCodec;
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (!Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    try {
                        final ProtocolHandler.Codec codec = field.getAnnotation(ProtocolHandler.Codec.class);
                        if (codec != null && field.getType() == StreamCodec.class) {
                            location = ResourceLocation.tryParse(codec.key());
                            streamCodec = (StreamCodec<FriendlyByteBuf, LeavesCustomPayload<?>>) field.get(null);
                            if (location != null) {
                                DECODERS.put(location, streamCodec);
                            }
                            ENCODERS.put(clazz, streamCodec);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                continue;
            }

            final LeavesProtocol.Register register = clazz.getAnnotation(LeavesProtocol.Register.class);
            if (register == null) {
                continue;
            }
            LeavesProtocol protocol;
            Set<Method> methods;
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                protocol = (LeavesProtocol) constructor.newInstance();
                Method[] publicMethods = clazz.getMethods();
                Method[] privateMethods = clazz.getDeclaredMethods();
                methods = new HashSet<>(publicMethods.length + privateMethods.length, 1.0f);
                Collections.addAll(methods, publicMethods);
                Collections.addAll(methods, privateMethods);
            } catch (Throwable throwable) {
                LOGGER.severe("Failed to load class " + clazz.getName() + ". " + throwable);
                return;
            }

            for (final Method method : methods) {
                if (method.isBridge() || method.isSynthetic()) {
                    continue;
                }

                method.setAccessible(true);

                final ProtocolHandler.Init init = method.getAnnotation(ProtocolHandler.Init.class);
                if (init != null) {
                    InvokerHolder<ProtocolHandler.Init> holder = new InvokerHolder<>(protocol, method, init);
                    try {
                        holder.invokeEmpty();
                    } catch (RuntimeException exception) {
                        LOGGER.severe("Failed to invoke init method " + method.getName() + " in " + clazz.getName() + ", " + exception.getCause() + ": " + exception.getMessage());
                    }
                    continue;
                }

                final ProtocolHandler.PayloadReceiver receiver = method.getAnnotation(ProtocolHandler.PayloadReceiver.class);
                if (receiver != null) {
                    String key = register.namespace() + ":" + receiver.key();
                    InvokerHolder<ProtocolHandler.PayloadReceiver> payloadReceiver = new InvokerHolder<>(protocol, method, receiver);
                    PAYLOAD_RECEIVERS.put(key, payloadReceiver);
                    ALL_KNOWN_ID.add(key);
                    continue;
                }

                final ProtocolHandler.BytebufReceiver receiver1 = method.getAnnotation(ProtocolHandler.BytebufReceiver.class);
                if (receiver1 != null) {
                    String key = register.namespace() + ":" + receiver1.key();
                    InvokerHolder<ProtocolHandler.BytebufReceiver> bytebufReceiver = new InvokerHolder<>(protocol, method, receiver1);
                    BYTEBUF_RECEIVERS.put(key, bytebufReceiver);
                    ALL_KNOWN_ID.add(key);
                    continue;
                }


                final ProtocolHandler.Ticker ticker = method.getAnnotation(ProtocolHandler.Ticker.class);
                if (ticker != null) {
                    TICKERS.add(new InvokerHolder<>(protocol, method, ticker));
                    continue;
                }

                final ProtocolHandler.PlayerJoin playerJoin = method.getAnnotation(ProtocolHandler.PlayerJoin.class);
                if (playerJoin != null) {
                    PLAYER_JOIN.add(new InvokerHolder<>(protocol, method, playerJoin));
                    continue;
                }

                final ProtocolHandler.PlayerLeave playerLeave = method.getAnnotation(ProtocolHandler.PlayerLeave.class);
                if (playerLeave != null) {
                    PLAYER_LEAVE.add(new InvokerHolder<>(protocol, method, playerLeave));
                    continue;
                }

                final ProtocolHandler.ReloadServer reloadServer = method.getAnnotation(ProtocolHandler.ReloadServer.class);
                if (reloadServer != null) {
                    RELOAD_SERVER.add(new InvokerHolder<>(protocol, method, reloadServer));
                    continue;
                }

                final ProtocolHandler.MinecraftRegister minecraftRegister = method.getAnnotation(ProtocolHandler.MinecraftRegister.class);
                if (minecraftRegister != null) {
                    InvokerHolder<ProtocolHandler.MinecraftRegister> invokerHolder = new InvokerHolder<>(protocol, method, minecraftRegister);
                    if (!minecraftRegister.ignoreId()) {
                        String key = register.namespace() + ":" + minecraftRegister.key();
                        KEYED_MINECRAFT_REGISTER.put(key, invokerHolder);
                        ALL_KNOWN_ID.add(key);
                    } else {
                        WILD_MINECRAFT_REGISTER.add(invokerHolder);
                    }
                }
            }
        }
        ALL_KNOWN_ID = ImmutableSet.copyOf(ALL_KNOWN_ID);
    }

    public static LeavesCustomPayload<?> decode(ResourceLocation id, FriendlyByteBuf buf) {
        var codec = DECODERS.get(id);
        if (codec == null) {
            return null;
        }
        return codec.decode(buf);
    }

    public static void encode(FriendlyByteBuf buf, LeavesCustomPayload<?> payload) {
        buf.writeResourceLocation(payload.id());
        var codec = ENCODERS.get(payload.getClass());
        if (codec == null) {
            return;
        }
        ENCODERS.get(payload.getClass()).encode(buf, payload);
    }

    public static void handlePayload(ServerPlayer player, LeavesCustomPayload<?> payload) {
        if (payload instanceof ErrorPayload errorPayload) {
            player.connection.disconnect(Component.literal("Payload " + Arrays.toString(errorPayload.packetID) + " from " + Arrays.toString(errorPayload.protocolID) + " error"), PlayerKickEvent.Cause.INVALID_PAYLOAD);
            return;
        }
        String key = payload.id().toString();
        InvokerHolder<ProtocolHandler.PayloadReceiver> holder;
        if ((holder = PAYLOAD_RECEIVERS.get(key)) != null) {
            holder.invokePayload(player, payload);
        }
    }

    public static void handleBytebuf(ServerPlayer player, ResourceLocation location, ByteBuf buf) {
        String key = location.toString();
        InvokerHolder<ProtocolHandler.BytebufReceiver> holder;
        if ((holder = BYTEBUF_RECEIVERS.get(key)) != null) {
            holder.invokeBuf(player, buf);
        }
    }

    public static void handleTick(long tickCount) {
        for (var ticker : TICKERS) {
            if (tickCount % ticker.handler().interval() == 0) {
                ticker.invokeEmpty();
            }
        }
    }

    public static void handlePlayerJoin(ServerPlayer player) {
        for (var join : PLAYER_JOIN) {
            join.invokePlayer(player);
        }
        ProtocolUtils.sendPayloadPacket(player, new FabricRegisterPayload(ALL_KNOWN_ID));
    }

    public static void handlePlayerLeave(ServerPlayer player) {
        for (var leave : PLAYER_LEAVE) {
            leave.invokePlayer(player);
        }
    }

    public static void handleServerReload() {
        for (var reload : RELOAD_SERVER) {
            reload.invokeEmpty();
        }
    }

    public static void handleMinecraftRegister(String channelId, ServerPlayer player) {
        InvokerHolder<ProtocolHandler.MinecraftRegister> keyedHolder = KEYED_MINECRAFT_REGISTER.get(channelId);
        String key = channelId.split(":")[1];
        if (keyedHolder != null) {
            keyedHolder.invokeString(player, key);
        }
        for (var wildHolder : WILD_MINECRAFT_REGISTER) {
            wildHolder.invokeString(player, key);
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

    }

    public record EmptyPayload(ResourceLocation id) implements LeavesCustomPayload<EmptyPayload> {

        @ProtocolHandler.Codec
        public static StreamCodec<FriendlyByteBuf, EmptyPayload> CODEC = StreamCodec.of(
            (buffer, value) -> {
            },
            buffer -> {
                throw new UnsupportedOperationException();
            }
        );

        @Override
        public ResourceLocation id() {
            return id;
        }
    }


    public record FabricRegisterPayload(Set<String> channels) implements LeavesCustomPayload<FabricRegisterPayload> {

        public static final ResourceLocation ID = ResourceLocation.tryParse("minecraft:register");

        @ProtocolHandler.Codec
        public static StreamCodec<FriendlyByteBuf, FabricRegisterPayload> CODEC = StreamCodec.of(
            FabricRegisterPayload::write,
            v -> {
                throw new UnsupportedOperationException();
            }
        );

        @Override
        public ResourceLocation id() {
            return ID;
        }

        public static void write(FriendlyByteBuf buf, FabricRegisterPayload payload) {
            boolean first = true;

            ResourceLocation channel;
            for (Iterator<String> var3 = payload.channels.iterator(); var3.hasNext(); buf.writeBytes(channel.toString().getBytes(StandardCharsets.US_ASCII))) {
                channel = ResourceLocation.parse(var3.next());
                if (first) {
                    first = false;
                } else {
                    buf.writeByte(0);
                }
            }
        }
    }
}