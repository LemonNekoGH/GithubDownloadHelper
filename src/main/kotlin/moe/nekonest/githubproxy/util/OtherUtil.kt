package moe.nekonest.githubproxy.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object OtherUtil {
    object Date{
        fun yyyyMMddHHmmss(): String{
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            return dateTimeFormatter.format(LocalDateTime.now())
        }
    }
}