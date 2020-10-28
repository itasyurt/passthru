package org.itasyurt.passthru

import org.itasyurt.passthru.logger.ConsoleLogger
import org.itasyurt.passthru.logger.PassthruLogger
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableZuulProxy
@SpringBootApplication
class PassthruApplication




fun main(args: Array<String>) {
    runApplication<PassthruApplication>(*args)
}


@Configuration
class Config {

    @Bean
    fun passthruLogger(): PassthruLogger = ConsoleLogger()

}

