package moe.nekonest.githubproxy

import com.alibaba.fastjson.JSON
import moe.nekonest.githubproxy.util.OldFileDeleteThread
import moe.nekonest.githubproxy.util.notExists
import org.apache.logging.log4j.LogManager
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.event.SpringApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.io.File

@Component
class ApplicationEventListener : ApplicationListener<SpringApplicationEvent> {
    private val logger = LogManager.getLogger()
    override fun onApplicationEvent(event: SpringApplicationEvent) {
        when (event) {
            is ApplicationReadyEvent -> onAppReady(event)
        }
    }

    private fun onAppReady(event: ApplicationReadyEvent) {
        logger.info("Spring Boot已启动")
        logger.info("正在读取配置文件")
        val configFile = File("./config.json")
        val configContent = if (configFile.notExists()){
            logger.info("未找到配置文件，使用默认配置")
            "{\"oldFileScanningIntervals\": 10, \"fileTimedOut\": 1440}"
        }else{
            String(configFile.readBytes())
        }
        val (intervals, timedOut) = JSON.parseObject(configContent,Configure::class.java)
        logger.info("旧文件扫描间隔为${intervals}分钟，旧文件过期时间为${timedOut}分钟")
        OldFileDeleteThread(intervals * 60 * 1000L, timedOut * 60 * 1000L).start()
        logger.info("旧文件扫描线程已启动")
        logger.info("服务器启动完毕")
    }
}