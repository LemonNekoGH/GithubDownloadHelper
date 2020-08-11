@file:Suppress("UNCHECKED_CAST")

package moe.nekonest.gdh.util

import com.alibaba.fastjson.JSONObject
import java.io.File
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

fun File.notExists() = !exists()

fun Session.sendJSON(vararg pairs: Pair<String, String>){
    val jsonObject = JSONObject()
    for (pair in pairs){
        jsonObject[pair.first] = pair.second
    }
    basicRemote.sendText(jsonObject.toJSONString())
}