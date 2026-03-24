package com.soul.app.soul_app_service.model

import okio.Path
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.sql.In

data class PsychologyCertificate(
    val id: Int,
    val psychologyId: Int,
    val path: String,
    )
