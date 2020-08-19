package moe.nekonest.gdh.ws

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.server.standard.ServerEndpointExporter

@Configuration
@EnableWebSocket
class WebSocketConfig {
    @Bean
    fun serverEndpoint() = ServerEndpointExporter()
}