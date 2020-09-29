package moe.nekonest.gdh.util

import java.io.File

val USER_HOME: String = System.getProperty("user.home")
val REPO_DIR = USER_HOME + File.separator + "repos"
val ARCHIVE_DIR = USER_HOME + File.separator + "archives"

object Size {
    const val BYTE = 1L
    const val KB = BYTE * 1024
    const val MB = KB * 1024
    const val GB = MB * 1024
    const val TB = GB * 1024
}

object Time {
    const val SECOND = 1000L
    const val MINUTE = 60 * SECOND
    const val HOUR = 60 * MINUTE
    const val DAY = 24 * HOUR
    const val WEEK = 7 * DAY
    const val YEAR = 365 * DAY
}