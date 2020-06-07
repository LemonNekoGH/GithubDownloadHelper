package moe.nekonest.githubproxy.util

import java.io.File

val USER_HOME: String = System.getProperty("user.home")
val REPO_DIR = USER_HOME + File.separator + "repos"
val ARCHIVE_DIR = USER_HOME + File.separator + "archives"

const val SIZE_B = 1L
const val SIZE_KB = SIZE_B * 1024
const val SIZE_MB = SIZE_KB * 1024
const val SIZE_GB = SIZE_MB * 1024
const val SIZE_TB = SIZE_GB * 1024