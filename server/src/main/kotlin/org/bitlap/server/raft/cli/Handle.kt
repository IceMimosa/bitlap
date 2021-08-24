package org.bitlap.server.raft.cli

import org.bitlap.common.proto.driver.BHandleIdentifier

/**
 * Abstract Handle
 *
 * @author 梦境迷离
 * @since 2021/6/6
 * @version 1.0
 */
abstract class Handle(private val handleId: HandleIdentifier = HandleIdentifier()) {

    open fun getHandleIdentifier(): HandleIdentifier = handleId

    constructor(bHandleIdentifier: BHandleIdentifier) : this(HandleIdentifier(bHandleIdentifier))

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + handleId.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other !is Handle) {
            return false
        }
        val otherHandle = other as Handle
        if (handleId != otherHandle.handleId) {
            return false
        }
        return true
    }

    abstract override fun toString(): String
}
