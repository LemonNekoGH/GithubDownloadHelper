package moe.nekonest.gdh

import com.alibaba.fastjson.JSONObject
import moe.nekonest.gdh.util.Time
import java.io.File

/**
 * 服务器配置文件的实体类
 */
class Configure {
    // 旧文件扫描间隔，单位：毫秒
    var oldFileScanningIntervals = Time.HOUR

    // 旧文件超时时长，单位：毫秒
    var fileTimedOut = Time.DAY

    fun load(file: File) {
        val fileContent = file.readText()
        val jsonObject = JSONObject.parseObject(fileContent)
    }
}