package moe.nekonest.gdh

import moe.lemonneko.nekogit.NekoGit
import moe.nekonest.gdh.util.ARCHIVE_DIR
import moe.nekonest.gdh.util.REPO_DIR
import moe.nekonest.gdh.util.Time
import moe.nekonest.gdh.util.deleteDir
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.env.Environment
import java.io.File
import java.io.PrintStream

@SpringBootApplication
class GDHApplication {
    object GDHBanner : Banner {
        override fun printBanner(environment: Environment?, sourceClass: Class<*>?, out: PrintStream?) {
            out ?: return
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


            val oldFileDeleteThread = Thread {
                println("old file delete job started")
                while (true) {
                    File(ARCHIVE_DIR).listFiles { f ->
                        System.currentTimeMillis() - f.lastModified() > Time.DAY
                    }?.forEach(File::deleteDir)
                    Thread.sleep(1000)
                }
            }

            oldFileDeleteThread.start()

            Runtime.getRuntime().addShutdownHook(Thread {
                NekoGit.destroy()
                File(REPO_DIR).deleteDir()
                println("old file delete job stopped")
            })

            val gdhApplication = SpringApplication(GDHApplication::class.java)
            gdhApplication.setBanner(GDHBanner)
            gdhApplication.run(*args)
        }
    }
}