package com.pharos.evlink.core

/** Result of a privileged shell command run through Shizuku/ADB. */
data class ShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
) {
    val isSuccess: Boolean get() = exitCode == 0
}

/** Generic outcome wrapper used across the vehicle layer. */
sealed interface Outcome<out T> {
    data class Ok<T>(val value: T) : Outcome<T>
    data class Err(val message: String, val cause: Throwable? = null) : Outcome<Nothing>
}
