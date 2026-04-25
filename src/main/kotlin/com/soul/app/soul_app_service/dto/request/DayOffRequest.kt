package com.soul.app.soul_app_service.dto.request

import java.sql.Date

data class DayOffRequest(
    val startTime : String,
    val endTime : String,
    val date: Date
)