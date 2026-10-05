package com.frostre1997.droidutility.terminal

import java.nio.ByteBuffer

object GhosttyVt {
    init {
        System.loadLibrary("droidutility_terminal")
    }

    external fun nativeCreate(cols: Int, rows: Int): Long
    external fun nativeFree(handle: Long)
    external fun nativeWrite(handle: Long, data: ByteArray): ByteArray?
    external fun nativeResize(handle: Long, cols: Int, rows: Int)
    external fun nativeSnapshot(handle: Long, buffer: ByteBuffer): Int
    external fun nativeEncodeKey(handle: Long, keyCode: Int, action: Int, metaState: Int, unshiftedCodepoint: Int, utf8: ByteArray?): ByteArray?
}
