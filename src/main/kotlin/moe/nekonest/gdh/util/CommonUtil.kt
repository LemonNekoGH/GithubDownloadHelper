package moe.nekonest.gdh.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CommonUtil {
    fun yyyyMMddHHmmss(): String {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        return dateTimeFormatter.format(LocalDateTime.now())
    }
}