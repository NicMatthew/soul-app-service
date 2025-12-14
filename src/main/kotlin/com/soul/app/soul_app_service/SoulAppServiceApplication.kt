package com.soul.app.soul_app_service

import io.swagger.v3.oas.models.OpenAPI
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class SoulAppServiceApplication

fun main(args: Array<String>) {
	runApplication<SoulAppServiceApplication>(*args)
}
