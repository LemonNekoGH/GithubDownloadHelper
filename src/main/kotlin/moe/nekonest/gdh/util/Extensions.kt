package moe.nekonest.gdh.util

import com.alibaba.fastjson.JSONObject
import moe.nekonest.gdh.workingthreads.CloneThread
import moe.nekonest.gdh.workingthreads.DownloadThread
import java.io.File
import java.io.IOException
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

fun File.deleteDir() {
    if (!exists()) {
        return
    }
    println("deleting $absolutePath")
    if (isDirectory) {
        val listFiles = listFiles()
        if (listFiles == null) {
            val deleted = delete()
            if (!deleted) {
                throw IOException("file cannot delete")
            }
        }
        listFiles!!.forEach {
            it.deleteDir()
        }
        val deleted = delete()
        if (!deleted) {
            throw IOException("file cannot delete")
        }
    } else {
        val deleted = delete()
        if (!deleted) {
            throw IOException("file cannot delete")
        }
    }
}

fun getAvg(vararg numbers: Int): Int {
    val numberOfNumbers = numbers.size
    var added = 0
    for (number in numbers) {
        added += number
    }
    return added / numberOfNumbers
}

fun newDownloadJob(uri: String) = DownloadThread(uri)
fun newCloneJob(uri: String) = CloneThread(uri)

enum class Status(val text: String) {
    CHECKING("checking"),
    PARSING("parsing"),
    CHECKING_OUT("checking out"),
    DOWNLOADING("downloading"),
    COMPRESSING("compressing"),
    COMPLETED("completed"),
    ERROR("error"),
    KEEP_ALIVE("keep alive")
}