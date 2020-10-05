package moe.nekonest.gdh.util

import com.alibaba.fastjson.JSONObject
import moe.nekonest.gdh.workingthreads.CloneCoroutine
import moe.nekonest.gdh.workingthreads.DownloadCoroutine
import java.net.URI
import javax.websocket.Session

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

fun newDownloadJob(uri: URI) = DownloadCoroutine(uri)
fun newCloneJob(uri: String) = CloneCoroutine(uri)

enum class Status(val text: String) {
    CHECKING("checking"),
    PARSING("parsing"),
    CHECKING_OUT("checking out"),
    DOWNLOADING("downloading"),
    COMPRESSING("compressing"),
    COMPLETED("completed"),
    ERROR("error")
}