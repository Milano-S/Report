package com.example.feedem.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.feedem.room.inbox.ReportDatabaseBuilder
import com.example.feedem.room.saved.SavedReportBuilder
import com.example.feedem.sharedPref.SharedPreference
import com.example.feedem.viewModel.ViewModelFeedem
import com.example.freedem.R
import com.example.freedem.databinding.FragmentSettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

private const val TAG = "SettingsFragment"

class SettingsFragment : Fragment() {

    //View Binding
    private lateinit var binding: FragmentSettingsBinding

    //ViewModel
    private val vm: ViewModelFeedem by activityViewModels()

    //Shared Pref
    private val sp: SharedPreference by lazy { SharedPreference(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_settings, container, false)
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

        binding = FragmentSettingsBinding.bind(view)
        binding.toolbarS.inflateMenu(R.menu.frag_menu)
        binding.toolbarS.setNavigationIcon(R.drawable.arrowback_2)
        binding.toolbarS.setNavigationOnClickListener { _ -> findNavController().popBackStack() }

        val dbI = ReportDatabaseBuilder.getInstance(requireContext()).ReportDao()
        val savedInboxItems = runBlocking { dbI.getAllReports() }
        var savedInboxCount = 0
        (savedInboxItems as MutableList).forEach { item ->
            if (File("/storage/emulated/0/Download/" + item.DateTitle + "-" + item.DocumentKey).isFile) {
                savedInboxCount++
            }
        }
        val savedItemCount = vm.savedReportList.size + savedInboxCount

        binding.tvFilesSaved.text = "Files Saved: $savedItemCount files"

        //Wifi Switch Button
        downloadOverWifi()

        //Download Inbox
        downloadInbox()

        //Clear Saved Files
        binding.btnClear.setOnClickListener {
            clearSavedFiles()
        }

        //Mark All as Read
        binding.btnMarkAsRead.setOnClickListener {
            markAllRead()
        }
    }

    private fun downloadOverWifi() {
        if (sp.getValueBoolean("downloadWifi", true)) {
            binding.swDownloadWifi.isChecked = true
        }
        binding.swDownloadWifi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sp.save("downloadWifi", true)
                Toast.makeText(requireContext(), "Download on Wi-Fi enabled", Toast.LENGTH_SHORT)
                    .show()
            } else {
                sp.save("downloadWifi", false)
                Toast.makeText(requireContext(), "Download on Wi-Fi disabled", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun downloadInbox() {
        if (sp.getValueString("inboxDownloadC").toString() == "Yes") {
            binding.swDownloadInbox.isChecked = true
        }
        binding.swDownloadInbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sp.save("inboxDownloadC", "Yes")
                Toast.makeText(requireContext(), "Download Inbox enabled", Toast.LENGTH_SHORT)
                    .show()
            } else {
                sp.save("inboxDownloadC", "No")
                Toast.makeText(requireContext(), "Download Inbox disabled", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun clearSavedFiles() {
        val dbI = SavedReportBuilder.getInstance(requireContext()).SavedReportDao()
        val dbI2 = ReportDatabaseBuilder.getInstance(requireContext()).ReportDao()

        val savedReports = runBlocking { dbI2.getAllReports() }
        val savedInbox = runBlocking { dbI.getSavedReports() }

        CoroutineScope(Dispatchers.IO).launch {
            savedInbox.forEach { report ->
                val reportFile =
                    File("/storage/emulated/0/Download/" + report.DateTitle + "-" + report.DocumentKey)
                reportFile.delete()
                dbI.deleteReport(report.DocumentKey)
            }
            dbI.deleteAll()
        }
        CoroutineScope(Dispatchers.IO).launch {
            savedReports.forEach { report ->
                val reportFile =
                    File("/storage/emulated/0/Download/" + report.DateTitle + "-" + report.DocumentKey)
                reportFile.delete()
                dbI2.deleteReport(report.DocumentKey)
            }
            dbI2.deleteAll()
        }

        binding.tvFilesSaved.text = getString(R.string.clear_items)
        Toast.makeText(requireContext(), "Items Cleared", Toast.LENGTH_SHORT).show()
    }

    private fun markAllRead() {
        val dbI = ReportDatabaseBuilder.getInstance(requireContext()).ReportDao()
        vm.inboxDocumentListV.forEach { item ->
            item.isRead = true
        }
        val inboxItems = runBlocking { dbI.getAllReports() }
        CoroutineScope(Dispatchers.IO).launch {
            inboxItems.forEach { inbox ->
                inbox.isRead = true
                dbI.updateReport(inbox)
            }
        }
        Toast.makeText(requireContext(), "All items Marked as Read", Toast.LENGTH_SHORT).show()
    }
}