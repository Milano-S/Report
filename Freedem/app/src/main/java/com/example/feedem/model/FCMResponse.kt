package com.exclr8.n1reportmanagement.model

data class FCMResponse(
    var Success: Boolean,
    var Offline: Boolean,
    var Status : Int,
    var Exception: Exception
)