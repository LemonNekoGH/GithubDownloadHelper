package moe.nekonest.gdh

import org.apache.catalina.connector.Connector
import org.apache.coyote.http11.Http11NioProtocol
import org.apache.tomcat.websocket.server.WsSci
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TomcatConfiguration {

    @Bean
    fun servletContainer(): ServletWebServerFactory {
        val tomcat = TomcatServletWebServerFactory()
        tomcat.addAdditionalTomcatConnectors(createSslConnector())
        return tomcat
    }

    private fun createSslConnector(): Connector {
        val connector = Connector(Http11NioProtocol::class.java.name)
        connector.scheme = "http"
        connector.port = 80
        connector.secure = false
        connector.redirectPort = 443
        return connector
    }

    @Bean
    fun tomcatContextCustomizer() = TomcatContextCustomizer {
        it.addServletContainerInitializer(WsSci(), null)
    }
}