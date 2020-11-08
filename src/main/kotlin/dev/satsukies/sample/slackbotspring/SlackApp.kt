package dev.satsukies.sample.slackbotspring

import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SlackApp(
  @Value("\${app.slack.bot-token}") val token: String,
  @Value("\${app.slack.signing-secret}") val secret: String
) {

  @Bean
  fun loadAppConfig(): AppConfig {
    return AppConfig.builder()
      .singleTeamBotToken(token)
      .signingSecret(secret)
      .build()
  }

  @Bean
  fun initSlackApp(config: AppConfig): App {

    val app = App(config)

    app.command("/hello") { req, context ->
      context.ack("Hello!")
    }

    return app
  }
}