package moe.nekonest.gdh

import org.apache.logging.log4j.LogManager
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.env.Environment
import java.io.PrintStream
@SpringBootApplication
class GDHApplication {
    private val logger = LogManager.getLogger()
    object GDHBanner : Banner {
        override fun printBanner(environment: Environment?, sourceClass: Class<*>?, out: PrintStream?) {
            out ?: throw IllegalArgumentException("param 'out' is null!")
            out.println("=========================================================")
            out.println("|                                                       |")
            out.println("|      GGGGGGGGGG      DDDDDDDDDD      HHH      HHH     |")
            out.println("|     GGG              DDD     DDD     HHH      HHH     |")
            out.println("|    GGG     GGGGGG    DDD      DDD    HHHHHHHHHHHH     |")
            out.println("|     GGG      GGG     DDD     DDD     HHH      HHH     |")
            out.println("|      GGGGGGGGGG      DDDDDDDDDD      HHH      HHH     |")
            out.println("|                                                       |")
            out.println("|   - Help You To Downloading Resources From Github -   |")
            out.println("|                                                       |")
            out.println("=========================================================")
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val gdhApplication = SpringApplication(GDHApplication::class.java)
            gdhApplication.setBanner(GDHApplication.GDHBanner)
            gdhApplication.run(*args)
        }
    }
}