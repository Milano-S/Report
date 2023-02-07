package com.example.feedem.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.feedem.model.ReportData
import com.example.feedem.model.SavedReportData
import com.example.feedem.room.saved.SavedReportBuilder
import com.example.feedem.viewModel.ViewModelFeedem
import com.example.freedem.R
import com.example.freedem.databinding.FragmentCustomerReportBinding
import com.exclr8.n1reportmanagement.adapter.ReportAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class CustomerReportFragment : Fragment() {

    //View Binding
    private lateinit var binding: FragmentCustomerReportBinding

    //ViewModel
    private val vm: ViewModelFeedem by activityViewModels()

    private lateinit var adapter: ReportAdapter

    private lateinit var customerReportList: List<ReportData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_customer_report, container, false)
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
        binding = FragmentCustomerReportBinding.bind(view)

        binding = FragmentCustomerReportBinding.bind(view)
        binding.toolbar.inflateMenu(R.menu.frag_menu)
        binding.toolbar.setNavigationIcon(R.drawable.arrowback_2)
        binding.toolbar.setNavigationOnClickListener {
            vm.customerReportsCpt.clear()
            vm.customerReportsCpt.clear()
            findNavController().popBackStack()
        }

        val savedReportDb = SavedReportBuilder.getInstance(requireContext()).SavedReportDao()

        customerReportList = if (vm.customerReportPage == "CT") {
            vm.customerReportsCpt
        } else {
            vm.customerReportsJhb
        }

        adapter = ReportAdapter(
            requireContext(), customerReportList, vm.checkForInternet(requireContext())
        )
        binding.rvCustomerReport.adapter = adapter

        adapter.setOnReportClickListener(object : ReportAdapter.onReportItemClickInterface {
            override fun onReportItemClick(position: Int) {
                val currentReport = customerReportList[position]
                vm.setCurrentReportV(currentReport)
                val savedDocument =
                    runBlocking { savedReportDb.getSavedReportUrl(currentReport.DocumentKey) }
                if (savedDocument == null) {
                    vm.setCurrentReportV(currentReport)
                    vm.setReporturl(currentReport.DocumentUrl)
                    findNavController().navigate(R.id.action_customerReportFragment_to_reportViewFragment)
                } else {
                    vm.setCurrentReportV(currentReport)
                    vm.setReporturl(savedDocument.DocumentUrl)
                    findNavController().navigate(R.id.action_customerReportFragment_to_reportViewFragment)
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
                        context = (activity as AppCompatActivity).applicationContext,
                        url = report.DocumentUrl,
                        title = report.DateTitle,
                        documentKey = report.DocumentKey
                    )
                }
            }
        })

        binding.rvCustomerReport.layoutManager = LinearLayoutManager(requireContext())

    }
}