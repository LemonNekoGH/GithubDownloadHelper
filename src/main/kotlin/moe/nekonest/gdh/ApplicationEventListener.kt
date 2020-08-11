package moe.nekonest.gdh

import org.apache.logging.log4j.LogManager
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.event.SpringApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class ApplicationEventListener : ApplicationListener<SpringApplicationEvent> {
    private val logger = LogManager.getLogger()
    override fun onApplicationEvent(event: SpringApplicationEvent) {
        when (event) {
            is ApplicationReadyEvent -> onAppReady(event)
        }
    }

    private fun onAppReady(event: ApplicationReadyEvent) {
        logger.info("Spring Boot核心已启动")

    }
}