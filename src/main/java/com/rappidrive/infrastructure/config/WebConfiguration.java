package com.rappidrive.infrastructure.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

/**
 * Web server configuration for Virtual Threads.
 * Configures Tomcat to use virtual threads for handling HTTP requests.
 * 
 * @see <a href="https://openjdk.org/jeps/444">JEP 444: Virtual Threads</a>
 */
@Configuration
public class WebConfiguration implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    
    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        // Enable virtual threads for Tomcat request handling
        // Every HTTP request will run on a virtual thread instead of a platform thread
        factory.addProtocolHandlerCustomizers(protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        });
    }
}
