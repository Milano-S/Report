package com.example.feedem.model

data class LoginDetails(
    val UserName: String,
    val Password: String,
    val OSName: String,
    val OSVersion: String,
    val DeviceBrand: String,
    val DeviceModel: String,
)