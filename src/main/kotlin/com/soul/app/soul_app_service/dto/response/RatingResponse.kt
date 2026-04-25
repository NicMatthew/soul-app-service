package com.soul.app.soul_app_service.dto.response

import java.sql.Timestamp

data class RatingResponse(

    val userProfilePicture:String?,
    val userName :String,
    val rate : Int,
    val description : String,
    val createdAt : Timestamp,
)
