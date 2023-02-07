package com.example.feedem.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.feedem.api.DocumentInterface
import com.example.feedem.model.DocumentResponse
import com.example.feedem.model.PeriodData
import com.example.feedem.model.ReportData
import com.example.feedem.model.SavedReportData
import com.example.feedem.room.inbox.ReportDatabaseBuilder
import com.example.feedem.room.saved.SavedReportBuilder
import com.example.feedem.url.Urls
import com.example.feedem.viewModel.ViewModelFeedem
import com.example.freedem.R
import com.example.freedem.databinding.FragmentDayBinding
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


private const val TAG = "DayFragment"
class DayFragment : Fragment() {

    //View Binding
    private lateinit var binding: FragmentDayBinding

    //ViewModel
    private val vm: ViewModelFeedem by activityViewModels()

    //Day Response
    private lateinit var dayResponse: List<ReportData>

    private lateinit var adapter: ReportAdapter

    private lateinit var fragContext : Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_day, container, false)
        setHasOptionsMenu(true)
        val pa = (activity as AppCompatActivity)
        val actionBar = pa.supportActionBar
        actionBar?.apply {
            title = ""
            hide()
        }
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentDayBinding.bind(view)
        binding.toolbar.inflateMenu(R.menu.frag_menu)
        binding.toolbar.setNavigationIcon(R.drawable.arrowback_2)
        binding.toolbar.setNavigationOnClickListener { _ -> findNavController().popBackStack() }
        fragContext = requireContext()

        getDayDocs()
        dayResponse = mutableListOf()
    }

    private fun getDayDocs() {

        val savedReportDb = SavedReportBuilder.getInstance(requireContext()).SavedReportDao()
        val db = ReportDatabaseBuilder
        val dbI = db.getInstance(requireContext()).ReportDao()

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

        val periodDate = vm.dayDate
        val retrofitData = retrofitBuilder.getReports(
            PeriodData("$periodDate 00:00:00", "1"),
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
                        (dayResponse as MutableList<ReportData>).addAll(mutableListOf(d))
                        vm.dayDocumentListV.addAll(mutableListOf(d))
                    }
                    dayResponse.forEach { item ->
                        if (item.DocumentKey in savedReportKey) {
                            item.isSaved = true
                        }
                    }
                    runBlocking {
                        adapter = ReportAdapter(
                            fragContext,
                            dayResponse,
                            vm.checkForInternet(fragContext)
                        )
                        binding.rvDay.adapter = adapter
                    }

                    adapter.setOnReportClickListener(object :
                        ReportAdapter.onReportItemClickInterface {
                        override fun onReportItemClick(position: Int) {
                            val currentReport = dayResponse[position]
                            val savedDocument =
                                runBlocking { savedReportDb.getSavedReportUrl(currentReport.DocumentKey) }
                            if (savedDocument == null) {
                                vm.setCurrentReportV(currentReport)
                                vm.setReporturl(currentReport.DocumentUrl)
                                findNavController().navigate(R.id.action_dayFragment_to_reportViewFragment)
                            } else {
                                vm.setCurrentReportV(currentReport)
                                vm.setReporturl(savedDocument.DocumentUrl)
                                findNavController().navigate(R.id.action_dayFragment_to_reportViewFragment)
                            }
                        }
                    })

                    adapter.saveReportPdf(object : ReportAdapter.OnSaveItemClick {
                        override fun saveReportPdf(position: Int) {
                            val report = dayResponse[position]
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
                    binding.rvDay.layoutManager = LinearLayoutManager(fragContext)
                }

                override fun onFailure(call: Call<DocumentResponse>, t: Throwable) {
                    Log.e(TAG, t.toString())
                }
            }
        )
    }

}
