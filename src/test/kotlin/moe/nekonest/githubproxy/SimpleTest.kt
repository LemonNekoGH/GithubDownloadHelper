package moe.nekonest.githubproxy

import moe.nekonest.githubproxy.util.ZipUtil
import org.junit.jupiter.api.Test
import java.io.File

class SimpleTest {
    @Test
    fun printUserHome(){
        println(System.getProperty("user.home"))
    }
    @Test
    fun getPathSeparator(){
        println(File.separator)
    }

    @Test
    fun zipTest(){
        ZipUtil.compress("C:\\Users\\LemonNeko\\archives\\DirTest")
    }
}