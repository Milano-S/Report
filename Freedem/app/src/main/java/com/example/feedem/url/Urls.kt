package com.example.feedem.url

import android.content.Context
import com.example.feedem.sharedPref.SharedPreference

class Urls(val context: Context) {

    var baseUrl = SharedPreference(context).getValueString("baseUrl").toString()

    companion object{

        const val appKey = "6348faea-bfa8-4c17-9768-964c5a9971ec"

        const val authenticationUrl = "/api/core/Authentication/Authorize"

        const val loginUrl = "api/core/Authentication/Login"

        const val inboxUrl = "api/oms/app/GetManagementReportInbox"

        const val documentUrl = "api/oms/app/GetManagementReports"
    }
}