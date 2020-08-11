package moe.nekonest.gdh

import moe.nekonest.gdh.util.ZipUtil
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