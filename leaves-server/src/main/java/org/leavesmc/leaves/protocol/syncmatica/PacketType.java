// LeavesMC - Protocol support for Syncmatica mod
// Copyright (c) 2024 LeavesMC
// Original source: https://github.com/piDemo/Syncmatica (LGPL-3.0 License)
// This file is licensed under the GNU General Public License v3.0.

package org.leavesmc.leaves.protocol.syncmatica;

import net.minecraft.resources.Identifier;

public enum PacketType {
    REGISTER_METADATA("register_metadata"),
    CANCEL_SHARE("cancel_share"),
    REQUEST_LITEMATIC("request_download"),
    SEND_LITEMATIC("send_litematic"),
    RECEIVED_LITEMATIC("received_litematic"),
    FINISHED_LITEMATIC("finished_litematic"),
    CANCEL_LITEMATIC("cancel_litematic"),
    REMOVE_SYNCMATIC("remove_syncmatic"),
    REGISTER_VERSION("register_version"),
    CONFIRM_USER("confirm_user"),
    FEATURE_REQUEST("feature_request"),
    FEATURE("feature"),
    MODIFY("modify"),
    MODIFY_REQUEST("modify_request"),
    MODIFY_REQUEST_DENY("modify_request_deny"),
    MODIFY_REQUEST_ACCEPT("modify_request_accept"),
    MODIFY_FINISH("modify_finish"),
    MESSAGE("mesage");

    public final Identifier identifier;

    PacketType(final String id) {
        identifier = Identifier.fromNamespaceAndPath(SyncmaticaProtocol.PROTOCOL_ID, id);
    }
}
