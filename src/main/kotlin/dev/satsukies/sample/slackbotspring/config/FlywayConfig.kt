package dev.satsukies.sample.slackbotspring.config

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlywayConfig {

  @Bean
  fun flywayMigrationStrategy(): FlywayMigrationStrategy {
    return FlywayMigrationStrategy { flyway ->
      flyway.clean()
      flyway.migrate()
    }
  }
}