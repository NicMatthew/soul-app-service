package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.service.TestService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/test")
class TestController(
    private val testService: TestService
) {

    @GetMapping("/hello")
    fun index(): String {
        return "Hello, Hans!"
    }

    @GetMapping("/database")
    fun database(): String {
        return testService.test()
    }
}