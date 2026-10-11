package com.frostre1997.droidutility.terminal

object Pty {
    init {
        System.loadLibrary("droidutility_terminal")
    }

    external fun nativeForkPty(
        binary: String,
        args: Array<String>,
        envVars: Array<String>,
        cwd: String,
        outFd: IntArray
    ): Int

    external fun nativeSetWindowSize(fd: Int, cols: Int, rows: Int)
    external fun nativeRead(fd: Int, buf: ByteArray): Int
    external fun nativeWrite(fd: Int, buf: ByteArray): Int
    external fun nativeCloseFd(fd: Int)
    external fun nativeWaitFor(pid: Int): Int
}
