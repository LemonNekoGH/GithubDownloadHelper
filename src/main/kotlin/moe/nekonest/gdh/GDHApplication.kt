package moe.nekonest.gdh

import com.alibaba.fastjson.JSONException
import moe.nekonest.gdh.util.ARCHIVE_DIR
import org.apache.logging.log4j.LogManager
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

// 本文件的全局Logger
private val logger = LogManager.getLogger()

@SpringBootApplication
class GDHApplication {
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
}

@Controller
class MainController {

    @RequestMapping("/file")
    fun getFile(fileName: String, request: HttpServletRequest, response: HttpServletResponse) {
        logger.info("收到下载请求，文件名是：$fileName")
        response.addHeader("Access-Control-Allow-Origin", "*")
        val fullPathFile = File(ARCHIVE_DIR, fileName)
        if (!fullPathFile.exists()) {
            logger.error("文件不存在")
            response.outputStream.write("{\"status\": \"failed\", \"errorCode\": \"404\"}".toByteArray())
        } else {
            logger.info("开始下载")
            response.addHeader("Content-Disposition", "attachment;filename=$fileName")
            response.contentType = "application/octet-stream"
            response.addHeader("Content-Length", fullPathFile.length().toString())
            val out = response.outputStream.buffered()
            out.write(fullPathFile.readBytes())
            out.flush()
            out.close()
            logger.info("下载结束")
        }
    }
}

object Main {
    /**
     * 程序可选参数
     * --help | -? 显示此帮助
     * --config | -c ${配置文件路径} 需要是json文件
     *
     * 开发人员注释：properties 键值对照
     * ================================================================
     *  键                值的类型  注释
     *  customConfigPath  布尔值    是否自定义配置文件路径
     *  useConfig         布尔值    是否使用配置文件
     *  config            对象      从配置文件读取后生成的Configure类对象
     * ================================================================
     */
    @JvmStatic
    fun main(args: Array<String>) {
//        val properties = Properties()
//
//        if (args.isEmpty()) {
//            properties["customConfigPath"] = false
//        }
//
//        if (args.contains("--help") || args.contains("-?")) {
//            printUsage()
//            return
//        }
//
//        if (args.contains("--config") || args.contains("-c")) {
//            if (!doConfig("--config", args, properties)) {
//                if (!doConfig("-c", args, properties)) {
//                    return
//                }
//            }
//        } else {
//            val defaultConfigFile = File("./config.json")
//            if (!defaultConfigFile.exists()) {
//                logger.debug("未找到默认配置文件，使用基本配置")
//            }
//        }

        val gdhApplication = SpringApplication(GDHApplication::class.java)
        gdhApplication.setBanner(GDHApplication.GDHBanner)
        gdhApplication.run(*args)
    }

    /**
     * 从文件获取配置
     */
    private fun doConfig(
            configArg: String,
            args: Array<String>,
            properties: Properties): Boolean {
        try {
            val index = args.indexOf(configArg)
            val configPath = args[index + 1]
            val configFile = File(configPath)
            if (configFile.exists()) {
                val configure = Configure()
                configure.load(configFile)
                properties["config"] = configure
            } else {
                println("找不到文件: ${configFile.absolutePath}，程序终止...")
                return false
            }
        } catch (e: IOException) {
            println(e.message)
            return false
        } catch (e: JSONException) {
            println("提供的配置文件不是标准JSON格式，程序终止...")
        }
        return false
    }

    /**
     * 显示程序用法
     */
    private fun printUsage() {
        println("用法: java -jar GithubDownloadHelper.jar [参数]")
        println("      --help | -? 显示此帮助信息")
        println("      --config [configPath] | -c [configPath] 使用指定路径上的配置文件")
    }
}