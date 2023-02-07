package com.example.feedem.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.feedem.api.DocumentInterface
import com.example.feedem.model.DocumentResponse
import com.example.feedem.model.PeriodData
import com.example.feedem.model.ReportData
import com.example.feedem.model.SavedReportData
import com.example.feedem.room.saved.SavedReportBuilder
import com.example.feedem.url.Urls
import com.example.feedem.viewModel.ViewModelFeedem
import com.example.freedem.R
import com.example.freedem.databinding.FragmentMonthBinding
import com.exclr8.n1reportmanagement.adapter.ReportAdapter
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
import java.time.Month
import java.util.*

class MonthFragment : Fragment() {

    //View Binding
    private lateinit var binding: FragmentMonthBinding

    //ViewModel
    private val vm: ViewModelFeedem by activityViewModels()

    //Month Response
    private lateinit var monthResponse: List<ReportData>

    //Customer Reports Cpt
    private lateinit var customerReportsCpt: List<ReportData>

    //Customer Reports Jhb
    private lateinit var customerReportsJhb: List<ReportData>

    private lateinit var adapter: ReportAdapter

    private lateinit var fragContext : Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_month, container, false)
        setHasOptionsMenu(true)
        val pa = (activity as AppCompatActivity)
        val actionBar = pa.supportActionBar
        actionBar?.apply {
            title = ""
            hide()
        }
        return v
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMonthBinding.bind(view)

        binding = FragmentMonthBinding.bind(view)
        binding.toolbarM.inflateMenu(R.menu.frag_menu)
        binding.toolbarM.setNavigationIcon(R.drawable.arrowback_2)
        binding.toolbarM.setNavigationOnClickListener { _ -> findNavController().popBackStack() }

        binding.llCustomerReports.isVisible = false
        binding.llReports.isVisible = false
        binding.rvMonth.isVisible = false
        binding.pbPdf.isVisible = true

        monthResponse = mutableListOf()
        customerReportsCpt = mutableListOf()
        customerReportsJhb = mutableListOf()

        fragContext = requireContext()

        val currentMonth = vm.dateInMillis
        val c = Calendar.getInstance()
        //Set time in milliseconds
        c.timeInMillis = currentMonth
        val mYear = c[Calendar.YEAR]
        val mMonth = c[Calendar.MONTH]
        val monthName = Month.of(mMonth + 1)

        binding.tvReportName.text = monthName.name + ", " + mYear.toString()
        binding.tvReportName2.text = monthName.name + ", " + mYear.toString()

        getMonthDocs()

        binding.cl1.setOnClickListener {
            vm.setCustomerReportPageVM("CT")
            findNavController().navigate(R.id.action_monthFragment_to_customerReportFragment)
        }
        binding.cl2.setOnClickListener {
            vm.setCustomerReportPageVM("JHB")
            findNavController().navigate(R.id.action_monthFragment_to_customerReportFragment)
        }
    }

    private fun getMonthDocs() {
        val savedReportDb = SavedReportBuilder.getInstance(requireContext()).SavedReportDao()
        val savedReports = runBlocking { savedReportDb.getSavedReports() }
        val savedReportKey = mutableListOf("")
        savedReports.forEach { report ->
            savedReportKey.addAll(listOf(report.DocumentKey))
        }

        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Urls(requireContext()).baseUrl)
            .build()
            .create(DocumentInterface::class.java)

        val monthDate = vm.monthDate
        val retrofitData = retrofitBuilder.getReports(
            PeriodData("$monthDate 00:00:00", "3"),
            vm.appTokenKey,
            vm.userTokenKey
        )

        retrofitData.enqueue(
            object : Callback<DocumentResponse> {
                override fun onResponse(
                    call: Call<DocumentResponse>,
                    response: Response<DocumentResponse>
                ) {
                    response.body()?.Reports?.forEach { d ->
                        if (d.ReportId != 8) {
                            (monthResponse as MutableList<ReportData>).addAll(mutableListOf(d))
                        }
                        vm.monthDocumentListV.addAll(mutableListOf(d))
                    }

                    response.body()?.Reports?.forEach { report ->
                        if (File("/storage/emulated/0/Download/" + report.DateTitle + "-" + report.DocumentKey).isFile) {
                            report.isSaved = true
                        }
                        if (report.ReportId == 8 && report.BranchId == 1) {
                            (customerReportsCpt as MutableList<ReportData>).addAll(
                                mutableListOf(
                                    report
                                )
                            )
                        }
                        if (report.ReportId == 8 && report.BranchId == 2) {
                            (customerReportsJhb as MutableList<ReportData>).addAll(
                                mutableListOf(
                                    report
                                )
                            )
                        }
                    }
                    vm.setCustomerReportsCptVM(customerReportsCpt)
                    vm.setCustomerReportsJhbVM(customerReportsJhb)

                    binding.llCustomerReports.isVisible = true
                    binding.llReports.isVisible = true
                    binding.rvMonth.isVisible = true
                    binding.pbPdf.isVisible = false

                    adapter = ReportAdapter(
                        fragContext,
                        monthResponse,
                        vm.checkForInternet(requireContext())
                    )

                    binding.rvMonth.adapter = adapter

                    adapter.setOnReportClickListener(object :
                        ReportAdapter.onReportItemClickInterface {
                        override fun onReportItemClick(position: Int) {
                            val currentReport = monthResponse[position]
                            vm.setCurrentReportV(currentReport)
                            val savedDocument =
                                runBlocking { savedReportDb.getSavedReportUrl(currentReport.DocumentKey) }
                            if (savedDocument == null) {
                                vm.setCurrentReportV(currentReport)
                                vm.setReporturl(currentReport.DocumentUrl)
                                findNavController().navigate(R.id.action_monthFragment_to_reportViewFragment)
                            } else {
                                vm.setCurrentReportV(currentReport)
                                vm.setReporturl(savedDocument.DocumentUrl)
                                findNavController().navigate(R.id.action_monthFragment_to_reportViewFragment)
                            }
                        }
                    })
                    adapter.saveReportPdf(object : ReportAdapter.OnSaveItemClick {
                        override fun saveReportPdf(position: Int) {
                            val report = adapter.reportList[position]
                            report.isSaved = true
                            CoroutineScope(Dispatchers.IO).launch {
                                savedReportDb.saveReport(
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
                    binding.rvMonth.layoutManager = LinearLayoutManager(requireContext())
                }

                override fun onFailure(call: Call<DocumentResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )
    }
}