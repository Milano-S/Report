package com.example.feedem.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.feedem.adapter.SavedAdapter
import com.example.feedem.model.SavedReportData
import com.example.feedem.room.inbox.ReportDatabaseBuilder
import com.example.feedem.viewModel.ViewModelFeedem
import com.example.freedem.R
import com.example.freedem.databinding.FragmentSavedBinding
import kotlinx.coroutines.runBlocking
import java.io.File

private const val TAG = "SavedFragment"
class SavedFragment : Fragment() {

    //View Binding
    private lateinit var binding: FragmentSavedBinding

    //ViewModel
    private val vm: ViewModelFeedem by activityViewModels()

    //Saved Reports
    //private lateinit var savedReportList: List<SavedReportData>

    //Rv Adapter
    private lateinit var adapter: SavedAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_saved, container, false)
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
        binding = FragmentSavedBinding.bind(view)

        val savedReportList = vm.savedReportList
        val vmSavedReportKeyList = mutableListOf<String>()
        savedReportList.forEach { item ->
            vmSavedReportKeyList.addAll(mutableListOf( item.DocumentKey))
        }
        val dbI = ReportDatabaseBuilder.getInstance(requireContext())
        val inboxReportList = runBlocking {
            dbI.ReportDao().getAllReports()
        }
        inboxReportList.forEach { item ->
            if (File("/storage/emulated/0/Download/" + item.DateTitle + "-" + item.DocumentKey).isFile && item.DocumentKey !in vmSavedReportKeyList) {
                item.isSaved = true
                savedReportList.addAll(
                    mutableListOf(
                        SavedReportData(
                            BranchId = item.BranchId,
                            BranchName = item.BranchName,
                            DateTitle = item.DateTitle,
                            DocumentKey = item.DocumentKey,
                            DocumentUrl = item.DocumentUrl,
                            FinancialPeriodId = item.FinancialPeriodId,
                            PeriodType = item.PeriodType,
                            ReportId = item.ReportId,
                            ReportName = item.ReportName,
                            savedLocation = item.DateTitle + "-" + item.DocumentKey,
                            isSaved = true
                        )
                    )
                )
                Log.i(TAG, item.toString())
            }
        }
        if (savedReportList.size == 0) {
            Toast.makeText(requireContext(), getString(R.string.no_saved), Toast.LENGTH_SHORT).show()
        }

        //Toolbar Menu
        binding.toolbarS.inflateMenu(R.menu.frag_menu)
        binding.toolbarS.setNavigationIcon(R.drawable.arrowback_2)
        binding.toolbarS.setNavigationOnClickListener { _ -> findNavController().popBackStack() }

        //Rv Adapter
        adapter = SavedAdapter(requireContext(), savedReportList)
        binding.rvSaved.adapter = adapter
        binding.rvSaved.layoutManager = LinearLayoutManager(requireContext())
        adapter.setOnSaveReportItemListener(object : SavedAdapter.OnSaveReportClick {
            override fun onSaveItemClick(position: Int) {
                val currentSavedReport = savedReportList[position]
                vm.setCurrentSavedReportV(currentSavedReport)
                vm.setReporturl(currentSavedReport.DocumentUrl)
                findNavController().navigate(R.id.action_savedFragment_to_reportViewFragment)
            }
        })
    }
}