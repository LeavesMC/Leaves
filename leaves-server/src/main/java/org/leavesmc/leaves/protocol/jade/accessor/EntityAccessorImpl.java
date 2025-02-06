package org.leavesmc.leaves.protocol.jade.accessor;

import com.google.common.base.Suppliers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.jade.JadeProtocol;
import org.leavesmc.leaves.protocol.jade.payload.RequestEntityPayload;
import org.leavesmc.leaves.protocol.jade.payload.ServerPayloadContext;
import org.leavesmc.leaves.protocol.jade.provider.IServerDataProvider;
import org.leavesmc.leaves.protocol.jade.util.CommonUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntityAccessorImpl extends AccessorImpl<EntityHitResult> implements EntityAccessor {

    private final Supplier<Entity> entity;

    public EntityAccessorImpl(Builder builder) {
        super(builder.level, builder.player, builder.serverData, builder.hit, builder.connected, builder.showDetails);
        entity = builder.entity;
    }

    public static void handleRequest(RequestEntityPayload message, ServerPayloadContext context, Consumer<CompoundTag> responseSender) {
        ServerPlayer player = context.player();
        context.execute(() -> {
            EntityAccessor accessor = message.data().unpack(player);
            if (accessor == null) {
                return;
            }
            Entity entity = accessor.getEntity();
            double maxDistance = Mth.square(player.entityInteractionRange() + 21);
            if (entity == null || player.distanceToSqr(entity) > maxDistance) {
                return;
            }
            List<IServerDataProvider<EntityAccessor>> providers = JadeProtocol.entityDataProviders.get(entity);
            CompoundTag tag = accessor.getServerData();
            for (IServerDataProvider<EntityAccessor> provider : providers) {
                if (!message.dataProviders().contains(provider)) {
                    continue;
                }
                try {
                    provider.appendServerData(tag, accessor);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            tag.putInt("EntityId", entity.getId());
            responseSender.accept(tag);
        });
    }

    @Override
    public Entity getEntity() {
        return CommonUtil.wrapPartEntityParent(getRawEntity());
    }

    @Override
    public Entity getRawEntity() {
        return entity.get();
    }

    @Override
    public ItemStack getPickedResult() {
        return null; //TODO implement minecraft pick up result
    }

    @NotNull
    @Override
    public Object getTarget() {
        return getEntity();
    }

    @Override
    public boolean verifyData(CompoundTag data) {
        if (!verify) {
            return true;
        }
        if (!data.contains("EntityId")) {
            return false;
        }
        return data.getInt("EntityId") == getEntity().getId();
    }

    public static class Builder implements EntityAccessor.Builder {

        public boolean showDetails;
        private Level level;
        private Player player;
        private CompoundTag serverData;
        private boolean connected;
        private Supplier<EntityHitResult> hit;
        private Supplier<Entity> entity;
        private boolean verify;

        @Override
        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        @Override
        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        @Override
        public Builder serverData(CompoundTag serverData) {
            this.serverData = serverData;
            return this;
        }

        @Override
        public Builder serverConnected(boolean connected) {
            this.connected = connected;
            return this;
        }

        @Override
        public Builder showDetails(boolean showDetails) {
            this.showDetails = showDetails;
            return this;
        }

        @Override
        public Builder hit(Supplier<EntityHitResult> hit) {
            this.hit = hit;
            return this;
        }

        @Override
        public Builder entity(Supplier<Entity> entity) {
            this.entity = entity;
            return this;
        }

        @Override
        public Builder from(EntityAccessor accessor) {
            level = accessor.getLevel();
            player = accessor.getPlayer();
            serverData = accessor.getServerData();
            connected = accessor.isServerConnected();
            showDetails = accessor.showDetails();
            hit = accessor::getHitResult;
            entity = accessor::getEntity;
            return this;
        }

        @Override
        public EntityAccessor.Builder requireVerification() {
            verify = true;
            return this;
        }

        @Override
        public EntityAccessor build() {
            EntityAccessorImpl accessor = new EntityAccessorImpl(this);
            if (verify) {
                accessor.requireVerification();
            }
            return accessor;
        }
    }

    public record SyncData(boolean showDetails, int id, int partIndex, Vec3 hitVec) {
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                SyncData::showDetails,
                ByteBufCodecs.VAR_INT,
                SyncData::id,
                ByteBufCodecs.VAR_INT,
                SyncData::partIndex,
                ByteBufCodecs.VECTOR3F.map(Vec3::new, Vec3::toVector3f),
                SyncData::hitVec,
                SyncData::new
        );

        public SyncData(EntityAccessor accessor) {
            this(
                    accessor.showDetails(),
                    accessor.getEntity().getId(),
                    CommonUtil.getPartEntityIndex(accessor.getRawEntity()),
                    accessor.getHitResult().getLocation());
        }

        public EntityAccessor unpack(ServerPlayer player) {
            Supplier<Entity> entity = Suppliers.memoize(() -> CommonUtil.getPartEntity(player.level().getEntity(id), partIndex));
            return new EntityAccessorImpl.Builder()
                    .level(player.level())
                    .player(player)
                    .showDetails(showDetails)
                    .entity(entity)
                    .hit(Suppliers.memoize(() -> new EntityHitResult(entity.get(), hitVec)))
                    .build();
        }
    }
}