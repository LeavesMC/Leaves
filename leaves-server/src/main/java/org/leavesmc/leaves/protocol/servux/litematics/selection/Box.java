package org.leavesmc.leaves.protocol.servux.litematics.selection;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import org.leavesmc.leaves.protocol.servux.litematics.utils.JsonUtils;
import org.leavesmc.leaves.protocol.servux.litematics.utils.PositionUtils;

import javax.annotation.Nullable;

public class Box
{
    @Nullable
    private BlockPos pos1;
    @Nullable
    private BlockPos pos2;
    private BlockPos size = BlockPos.ZERO;
    private String name = "Unnamed";
    private PositionUtils.Corner selectedCorner = PositionUtils.Corner.NONE;

    public Box()
    {
        this.pos1 = BlockPos.ZERO;
        this.pos2 = BlockPos.ZERO;
        this.updateSize();
    }

    public Box(@Nullable BlockPos pos1, @Nullable BlockPos pos2, String name)
    {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.name = name;

        this.updateSize();
    }

    public Box copy()
    {
        Box box = new Box(this.pos1, this.pos2, this.name);
        box.setSelectedCorner(this.selectedCorner);
        return box;
    }

    @Nullable
    public BlockPos getPos1()
    {
        return this.pos1;
    }

    @Nullable
    public BlockPos getPos2()
    {
        return this.pos2;
    }

    public BlockPos getSize()
    {
        return this.size;
    }

    public String getName()
    {
        return this.name;
    }

    public void setPos1(@Nullable BlockPos pos)
    {
        this.pos1 = pos;
        this.updateSize();
    }

    public void setPos2(@Nullable BlockPos pos)
    {
        this.pos2 = pos;
        this.updateSize();
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setSelectedCorner(PositionUtils.Corner corner)
    {
        this.selectedCorner = corner;
    }

    private void updateSize()
    {
        if (this.pos1 != null && this.pos2 != null)
        {
            this.size = PositionUtils.getAreaSizeFromRelativeEndPosition(this.pos2.subtract(this.pos1));
        } else if (this.pos1 == null && this.pos2 == null)
        {
            this.size = BlockPos.ZERO;
        } else
        {
            this.size = new BlockPos(1, 1, 1);
        }
    }

    public BlockPos getPosition(PositionUtils.Corner corner)
    {
        return corner == PositionUtils.Corner.CORNER_1 ? this.getPos1() : this.getPos2();
    }

    @Nullable
    public static Box fromJson(JsonObject obj)
    {
        if (JsonUtils.hasString(obj, "name"))
        {
            BlockPos pos1 = JsonUtils.blockPosFromJson(obj, "pos1");
            BlockPos pos2 = JsonUtils.blockPosFromJson(obj, "pos2");

            if (pos1 != null || pos2 != null)
            {
                Box box = new Box();
                box.setName(obj.get("name").getAsString());

                if (pos1 != null)
                {
                    box.setPos1(pos1);
                }

                if (pos2 != null)
                {
                    box.setPos2(pos2);
                }

                return box;
            }
        }

        return null;
    }

    @Nullable
    public JsonObject toJson()
    {
        JsonObject obj = new JsonObject();

        if (this.pos1 != null)
        {
            obj.add("pos1", JsonUtils.blockPosToJson(this.pos1));
        }

        if (this.pos2 != null)
        {
            obj.add("pos2", JsonUtils.blockPosToJson(this.pos2));
        }

        obj.add("name", new JsonPrimitive(this.name));

        return this.pos1 != null || this.pos2 != null ? obj : null;
    }

    public BlockBox toVanilla()
    {
        if (pos1 != null && pos2 != null)
        {

            return new BlockBox(pos1, pos2);
        }
        return null;
    }
}
