package com.example.feedem.model

data class PushNotificationRequest(
    var DeviceTypeId: Int,
    var DeviceUID: String ,
    var DeviceName: String ,
    var DeviceDescription: String ,
    var Token: String
)
