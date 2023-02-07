package com.example.feedem.fragment

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.feedem.adapter.InboxAdapter
import com.example.feedem.api.InboxInterface
import com.example.feedem.model.DocumentResponse
import com.example.feedem.model.FromDate
import com.example.feedem.model.ReportData
import com.example.feedem.model.SavedReportData
import com.example.feedem.room.inbox.ReportDatabaseBuilder
import com.example.feedem.room.saved.SavedReportBuilder
import com.example.feedem.sharedPref.SharedPreference
import com.example.feedem.url.Urls
import com.example.feedem.viewModel.ViewModelFeedem
import com.example.freedem.R
import com.example.freedem.databinding.FragmentInboxBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
private const val STORAGE_PERMISSION_CODE = 101
//private const val DOCUMENT_URL = "https://n1oms-uat.azurewebsites.net/"
private const val TAG = "InboxFragment"
class InboxFragment : Fragment() {

    //ViewModel
    private val vm: ViewModelFeedem by activityViewModels()

    //View Binding
    private lateinit var binding: FragmentInboxBinding

    private lateinit var reportList: List<ReportData>

    private lateinit var adapter: InboxAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_inbox, container, false)
        setHasOptionsMenu(true)

        val pa = (activity as AppCompatActivity)
        val actionBar = pa.supportActionBar
        actionBar?.apply {
            title = ""
            hide()
        }
        if (!vm.checkForInternet(requireContext())) {
            vm.checkPermission(
                READ_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE,
                requireActivity(),
                requireContext()
            )
        }


        val db = ReportDatabaseBuilder
        val dbI = db.getInstance(requireContext())

        val savedInboxList = runBlocking {
            dbI.ReportDao().getAllReports()
        }

        reportList = mutableListOf()
        savedInboxList.forEach { item ->
            if (File("/storage/emulated/0/Download/" + item.DateTitle + "-" + item.DocumentKey).isFile && !vm.checkForInternet(requireContext())
            ) {
                (reportList as MutableList<ReportData>).addAll(mutableListOf(item))
            } else if (vm.checkForInternet(requireContext())) {
                (reportList as MutableList<ReportData>).addAll(mutableListOf(item))
            }
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = ReportDatabaseBuilder
        val dbI = db.getInstance(requireContext()).ReportDao()
        val savedReportDb = SavedReportBuilder.getInstance(requireContext()).SavedReportDao()

        binding = FragmentInboxBinding.bind(view)

        binding.apply {
            pbPdf.isVisible = false
            rvInbox.isVisible = true
        }

        //Toolbar Menu
        binding.toolbarI.inflateMenu(R.menu.frag_menu)
        binding.toolbarI.setNavigationIcon(R.drawable.arrowback_2)
        binding.toolbarI.setNavigationOnClickListener { _ -> findNavController().popBackStack() }

        reportList.forEach { item ->
            if (File("/storage/emulated/0/Download/" + item.DateTitle + "-" + item.DocumentKey).isFile) {
                item.isSaved = true
            }
        }
        //Rv adapter
        adapter = if (reportList.isEmpty() && vm.checkForInternet(requireContext())) {
            vm.inboxDocumentListV.forEach { item ->
                (reportList as MutableList<ReportData>).addAll(mutableListOf(item))
            }
            CoroutineScope(Dispatchers.IO).launch {
                dbI.insertAllReports(reportList)
            }
            InboxAdapter(
                requireContext(),
                vm.inboxDocumentListV,
                vm.checkForInternet(requireContext())
            )
        } else {
            InboxAdapter(requireContext(), reportList, vm.checkForInternet(requireContext()))
        }

        if (reportList.isEmpty() && !vm.checkForInternet(requireContext())) {
            Toast.makeText(requireContext(), getString(R.string.no_inbox), Toast.LENGTH_SHORT)
                .show()
        }

        binding.rvInbox.adapter = adapter
        binding.rvInbox.layoutManager = LinearLayoutManager(requireContext())

        adapter.setOnReportClickListener(object : InboxAdapter.OnReportItemClickInterface {
            override fun onReportItemClick(position: Int) {
                val report = reportList[position]
                report.isRead = true
                runBlocking { dbI.updateReport(report) }
                val savedDocument =
                    runBlocking { savedReportDb.getSavedReportUrl(report.DocumentKey) }
                if (savedDocument == null) {
                    vm.setCurrentReportV(report)
                    vm.setReporturl(report.DocumentUrl)
                    vm.setCurrentSavedReportV(
                        SavedReportData(
                            BranchId = report.BranchId,
                            BranchName = report.BranchName,
                            DateTitle = report.DateTitle,
                            DocumentKey = report.DocumentKey,
                            DocumentUrl = report.DocumentUrl,
                            FinancialPeriodId = report.FinancialPeriodId,
                            PeriodType = report.PeriodType,
                            ReportId = report.ReportId,
                            ReportName = report.ReportName,
                            savedLocation = report.DateTitle + "-" + report.DocumentKey,
                            isSaved = true
                        )
                    )
                    findNavController().navigate(R.id.action_inboxFragment_to_reportViewFragment)
                } else {
                    vm.setCurrentReportV(report)
                    vm.setReporturl(savedDocument.DocumentUrl)
                    vm.setCurrentSavedReportV(
                        SavedReportData(
                            BranchId = report.BranchId,
                            BranchName = report.BranchName,
                            DateTitle = report.DateTitle,
                            DocumentKey = report.DocumentKey,
                            DocumentUrl = report.DocumentUrl,
                            FinancialPeriodId = report.FinancialPeriodId,
                            PeriodType = report.PeriodType,
                            ReportId = report.ReportId,
                            ReportName = report.ReportName,
                            savedLocation = report.DateTitle + "-" + report.DocumentKey,
                            isSaved = true
                        )
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        dbI.updateReport(report)
                    }
                    findNavController().navigate(R.id.action_inboxFragment_to_reportViewFragment)
                }
            }
        })
        adapter.saveReportPdf(object : InboxAdapter.OnSaveItemClick {
            val dbI = ReportDatabaseBuilder.getInstance(requireContext())
            override fun saveReportPdf(position: Int) {
                val report = reportList[position]
                report.isSaved = true
                CoroutineScope(Dispatchers.IO).launch {
                    dbI.insertReport(report)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    vm.downloadPdf(
                        context = requireContext(),
                        url = report.DocumentUrl,
                        title = report.DateTitle,
                        documentKey = report.DocumentKey
                    )
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Storage Permission Granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Storage Permission Denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    //Get Inbox
    private fun getReportInbox(FromDate: FromDate, appTokenKey: String, userTokenKey: String) {

        val db = ReportDatabaseBuilder
        val dbI = db.getInstance(requireContext()).ReportDao()
        val sp = SharedPreference(requireContext())

        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Urls(requireContext()).baseUrl)
            .build()
            .create(InboxInterface::class.java)

        val retrofitData = retrofitBuilder.getInbox(
            FromDate,
            appTokenKey,
            userTokenKey
        )

        retrofitData.enqueue(
            object : Callback<DocumentResponse> {
                override fun onResponse(
                    call: Call<DocumentResponse>,
                    response: Response<DocumentResponse>
                ) {
                    reportList = response.body()!!.Reports
                    CoroutineScope(Dispatchers.IO).launch {
                        dbI.insertAllReports(reportList)
                    }
                }

                override fun onFailure(call: Call<DocumentResponse>, t: Throwable) {
                    Log.e(TAG, t.message.toString())
                }
            }
        )
    }

}
