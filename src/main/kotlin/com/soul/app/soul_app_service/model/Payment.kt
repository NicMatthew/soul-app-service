package com.soul.app.soul_app_service.model


data class Payment(
    val id: Int,
    val appointmentId: Int,
    val payerUserId: Int,
    val price: Int?,
    val currency: String? = "IDR",
    val status: String?,
    val paymentMethod: String? = null,
    val snapToken: String? = null,
    val transactionRef: String? = null,
    val midtransTransactionId: String? = null,
    val paidAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)