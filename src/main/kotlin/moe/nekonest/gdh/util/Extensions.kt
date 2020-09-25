@file:Suppress("UNCHECKED_CAST")

package moe.nekonest.gdh.util

import com.alibaba.fastjson.JSONObject
import java.io.OutputStream
import javax.websocket.Session

operator fun String.compareTo(outputStream: OutputStream): Int {
    val buffer = outputStream.buffered()
    buffer.write(toByteArray())
    buffer.flush()
    return 0
}

fun Session.sendJSON(vararg pairs: Pair<String, String>) {
    val jsonObject = JSONObject()
    for (pair in pairs) {
        jsonObject[pair.first] = pair.second
    }
    basicRemote.sendText(jsonObject.toJSONString())
}

fun Session.sendStatus(status: Status, text: String? = null) {
    if (text == null) {
        sendJSON("status" to status.text)
    } else {
        sendJSON(
                "status" to status.text,
                "text" to text
        )
    }
}

enum class Status(val text: String) {
    CHECKING("checking"),
    PARSING("parsing"),
    CHECKING_OUT("checking out"),
    DOWNLOADING("downloading"),
    COMPRESSING("compressing"),
    COMPLETED("completed"),
    ERROR("error")
}