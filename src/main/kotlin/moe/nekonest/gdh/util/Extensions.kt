@file:Suppress("UNCHECKED_CAST")

package moe.nekonest.gdh.util

import com.alibaba.fastjson.JSONObject
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.io.File
import java.io.OutputStream

operator fun String.compareTo(outputStream: OutputStream): Int {
    val buffer = outputStream.buffered()
    buffer.write(toByteArray())
    buffer.flush()
    return 0
}

private val WebSocketSession.attributes: HashMap<String, Any?>
    get() = HashMap()

fun <T> WebSocketSession.getAttribute(name: String): T? {
    val attr = attributes[name] ?: return null
    return attr as T
}

fun File.notExists() = !exists()

fun WebSocketSession.sendJSON(vararg pairs: Pair<String, String>) {
    val jsonObject = JSONObject()
    for (pair in pairs) {
        jsonObject[pair.first] = pair.second
    }
    sendMessage(TextMessage(jsonObject.toJSONString()))
}