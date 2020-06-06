package moe.nekonest.githubproxy.util

import java.io.File

val USER_HOME: String = System.getProperty("user.home")
val REPO_DIR = USER_HOME + File.separator + "repos"
val ARCHIVE_DIR = USER_HOME + File.separator + "archives"

const val SLEEP_TIME = 1000L * 60 * 10
const val FILE_TIME_OUT = 1000 * 60 * 60 * 24