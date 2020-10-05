package moe.nekonest.gdh

import moe.lemonneko.nekogit.NekoGit
import moe.nekonest.gdh.util.ARCHIVE_DIR
import moe.nekonest.gdh.util.REPO_DIR
import org.apache.logging.log4j.LogManager
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.env.Environment
import java.io.File
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
            NekoGit.init()
            val gdhApplication = SpringApplication(GDHApplication::class.java)
            gdhApplication.setBanner(GDHBanner)
            gdhApplication.run(*args)
            NekoGit.destroy()
            val repo = File(REPO_DIR)
            if (repo.exists()) {
                repo.delete()
            }
            val archive = File(ARCHIVE_DIR)
            if (archive.exists()) {
                archive.delete()
            }
        }
    }
}