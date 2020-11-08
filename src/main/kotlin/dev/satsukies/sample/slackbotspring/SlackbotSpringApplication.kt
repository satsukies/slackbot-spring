package dev.satsukies.sample.slackbotspring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SlackbotSpringApplication

fun main(args: Array<String>) {
	runApplication<SlackbotSpringApplication>(*args)
}
