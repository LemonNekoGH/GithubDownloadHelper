@file:Suppress("UNCHECKED_CAST")

package moe.nekonest.githubproxy.util

import java.io.OutputStream
import javax.websocket.Session

operator fun String.compareTo(outputStream: OutputStream): Int {
    val buffer = outputStream.buffered()
    buffer.write(toByteArray())
    buffer.flush()
    return 0
}

private val Session.attributes: HashMap<String, Any?>
    get() = HashMap()

fun <T> Session.getAttribute(name: String): T? {
    val attr = attributes[name] ?: return null
    return attr as T
}