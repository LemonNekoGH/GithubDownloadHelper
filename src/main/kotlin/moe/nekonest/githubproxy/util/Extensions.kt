package moe.nekonest.githubproxy.util

import java.io.OutputStream

operator fun String.compareTo(outputStream: OutputStream): Int{
    val buffer = outputStream.buffered()
    buffer.write(toByteArray())
    buffer.flush()
    return 0
}