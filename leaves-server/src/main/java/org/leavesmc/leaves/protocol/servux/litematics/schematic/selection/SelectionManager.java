package org.leavesmc.leaves.protocol.servux.litematics.schematic.selection;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import com.google.gson.JsonElement;

import fi.dy.masa.servux.util.JsonUtils;

public class SelectionManager
{
    private final Map<String, AreaSelection> selections = new HashMap<>();
    private final Map<String, AreaSelection> readOnlySelections = new HashMap<>();
    @Nullable
    private String currentSelectionId;
    private SelectionMode mode = SelectionMode.SIMPLE;

    @Nullable
    public String getCurrentSelectionId()
    {
        return this.mode == SelectionMode.NORMAL ? this.currentSelectionId : null;
    }

    @Nullable
    public String getCurrentNormalSelectionId()
    {
        return this.currentSelectionId;
    }

    @Nullable
    protected AreaSelection getNormalSelection(@Nullable String selectionId)
    {
        return selectionId != null ? this.selections.get(selectionId) : null;
    }

    @Nullable
    private AreaSelection tryLoadSelectionFromFile(String selectionId)
    {
        return tryLoadSelectionFromFile(Path.of(selectionId));
    }

    @Nullable
    public static AreaSelection tryLoadSelectionFromFile(Path file)
    {
        JsonElement el = JsonUtils.parseJsonFileAsPath(file);

        if (el != null && el.isJsonObject())
        {
            return AreaSelection.fromJson(el.getAsJsonObject());
        }

        return null;
    }


    public void clear()
    {
        this.currentSelectionId = null;
        this.selections.clear();
        this.readOnlySelections.clear();
    }

}
