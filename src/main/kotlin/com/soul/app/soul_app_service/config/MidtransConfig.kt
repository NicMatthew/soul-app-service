package com.soul.app.soul_app_service.config

import com.midtrans.Config
import com.midtrans.service.MidtransSnapApi
import com.midtrans.service.impl.MidtransSnapApiImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MidtransConfig(

    @Value("\${midtrans.server-key}")
    private val serverKey: String,

    @Value("\${midtrans.is-production}")
    private val isProduction: Boolean

) {

    @Bean
    fun midtransSnapApi(): MidtransSnapApi {
        return MidtransSnapApiImpl(
            Config.builder()
            .setServerKey(serverKey)
            .setIsProduction(isProduction)
            .build()
        )
    }
}