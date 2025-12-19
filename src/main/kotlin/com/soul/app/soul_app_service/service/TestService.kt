package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.repository.TestRepository
import org.springframework.stereotype.Service

@Service
class TestService (
    private val testRepository: TestRepository
) {
    fun test(): String{
        return testRepository.testRepository()
    }
}