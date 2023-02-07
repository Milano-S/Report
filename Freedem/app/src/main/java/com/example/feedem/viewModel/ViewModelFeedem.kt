package com.example.feedem.viewModel

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.feedem.model.ReportData
import com.example.feedem.model.SavedReportData
import com.example.feedem.url.Urls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL

class ViewModelFeedem : ViewModel() {

    var baseUrl = Urls.urlBase
    fun setBaseUrlVM(url: String) {
        baseUrl = url
    }


    //App Token Key
    var appTokenKey = ""
    fun setAppTokenKeyV(appToken: String) {
        appTokenKey = appToken
    }

    //User Token Key
    var userTokenKey = ""
    fun setUserTokenKeyV(userToken: String) {
        userTokenKey = userToken
    }

    //FCM Token
    var fcmToken = ""
    fun setFcmTokenV(FcmToken: String) {
        fcmToken = FcmToken
    }

    //Day Date
    var dayDate = ""
    fun setDayDateV(day: String) {
        dayDate = day
    }

    //Week Date
    var weekDate = ""
    fun setWeekDateV(week: String) {
        weekDate = week
    }

    //Month Date
    var monthDate = ""
    fun setMonthDateV(monthDate: String) {
        this.monthDate = monthDate
    }

    //List of Inbox documents
    var inboxDocumentListV = mutableListOf<ReportData>()
    fun setInboxDocuments(documentList: List<ReportData>) {
        documentList.forEach { document ->
            inboxDocumentListV.addAll(mutableListOf(document))
        }
    }

    //List of Day documents
    var dayDocumentListV = mutableListOf<ReportData>()
    fun setDayDocuments(dayResponse: List<ReportData>) {
        dayResponse.forEach { document ->
            dayDocumentListV.addAll(mutableListOf(document))
        }
    }

    //List of Week documents
    var weekDocumentListV = mutableListOf<ReportData>()
    fun setWeekDocuments(weekResponse: List<ReportData>) {
        weekResponse.forEach { document ->
            dayDocumentListV.addAll(mutableListOf(document))
        }
    }

    //List of Month documents
    var monthDocumentListV = mutableListOf<ReportData>()
    fun setMonthDocuments(monthResponse: List<ReportData>) {
        monthResponse.forEach { document ->
            dayDocumentListV.addAll(mutableListOf(document))
        }
    }

    //Current ReportItem
    var currentReport: ReportData? = null
    fun setCurrentReportV(report: ReportData) {
        currentReport = report
    }

    //Current SavedReportItem
    var currentSavedReport: SavedReportData? = null
    fun setCurrentSavedReportV(savedReport: SavedReportData) {
        currentSavedReport = savedReport
    }

    //Saved Report Items
    var savedReportList = mutableListOf<SavedReportData>()
    fun setSavedReportLisV(savedList: List<SavedReportData>) {
        savedList.forEach { sr ->
            savedReportList.addAll(mutableListOf(sr))
        }
    }

    //Customer Reports Cpt
    var customerReportsCpt = mutableListOf<ReportData>()
    fun setCustomerReportsCptVM(customerReportList: List<ReportData>) {
        customerReportList.forEach { report ->
            customerReportsCpt.addAll(mutableListOf(report))
        }
    }

    //Customer Reports Cpt
    var customerReportsJhb = mutableListOf<ReportData>()
    fun setCustomerReportsJhbVM(customerReportList: List<ReportData>) {
        customerReportList.forEach { report ->
            customerReportsJhb.addAll(mutableListOf(report))
        }
    }

    //Report Pdf Url
    var reportUrl = ""
    fun setReporturl(url: String) {
        reportUrl = url
    }

    //Report Pdf
    var reportPdf = ""
    fun setReportPdfInfo(pdfInfo: String) {
        reportPdf = pdfInfo
    }

    //Customer Report Url
    var customerReportPage = ""
    fun setCustomerReportPageVM(page: String) {
        customerReportPage = page
    }

    //Date In Millis
    var dateInMillis: Long = 0
    fun setDateInMillis(dateF: Long?) {
        if (dateF != null) {
            dateInMillis = dateF
        }
    }

    //Download Pdf
    suspend fun downloadPdf(
        context: Context,
        url: String?,
        title: String,
        documentKey: String
    ): Long {

        val doesPdfExist = checkIfPdfExists(url.toString())
        if (!doesPdfExist || File("/storage/emulated/0/Download/$title-$documentKey").isFile) {
            return 0L
        } else {
            val downloadReference: Long
            val dm: DownloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri)

            //Pdf Save Location
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "$title-$documentKey"
            )
           // request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setTitle("$title-$documentKey")
            downloadReference = dm.enqueue(request)

            return downloadReference
        }
    }

    private suspend fun checkIfPdfExists(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val u = URL(url)
                val urlCon = u.openConnection()
                val br = BufferedReader(InputStreamReader(urlCon.getInputStream()))
                return@withContext true
            } catch (e: Exception) {
                Log.i("View", e.message.toString())
                return@withContext false
            }
        }
    }

    //Check Internet Connection
    fun checkForInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    //Check if Using Wifi
    fun isNotUsingWifi(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                //activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    // Function to check and request permission.
    fun checkPermission(
        permission: String,
        requestCode: Int,
        activity: Activity,
        context: Context
    ) {
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        } else {
            ///
        }
    }
}