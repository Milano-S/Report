package com.example.feedem.fragment

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.feedem.room.saved.SavedReportBuilder
import com.example.feedem.viewModel.ViewModelFeedem
import com.example.freedem.R
import com.example.freedem.databinding.FragmentReportViewBinding
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

private const val TAG = "ReportViewFragment"
private const val STORAGE_PERMISSION_CODE = 101

class ReportViewFragment : Fragment() {

    //View Binding
    private lateinit var binding: FragmentReportViewBinding

    //ViewModel
    private val vm: ViewModelFeedem by activityViewModels()

    val pdfView: PDFView by lazy {
        requireView().findViewById(R.id.pdfView)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_report_view, container, false)
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

        val currentReport = vm.currentReport
        val pdfUrl = vm.reportUrl

        val currentSavedReport = vm.currentSavedReport

        binding = FragmentReportViewBinding.bind(view)
        binding.toolbar.inflateMenu(R.menu.frag_menu)
        binding.toolbar.setNavigationIcon(R.drawable.arrowback_2)
        binding.toolbar.setNavigationOnClickListener { _ -> findNavController().popBackStack() }
        binding.btnShare.setOnClickListener {
            if (vm.checkForInternet(requireContext())) {
                shareDocument(pdfUrl)
            } else {
                Toast.makeText(requireContext(), "Offline", Toast.LENGTH_SHORT).show()
            }
        }

        if (vm.checkForInternet(requireContext())) {
            RetrievePDFFromURL(
                pdfView = pdfView,
                pbPdf = binding.pbPdf,
                errorLayout = binding.ll404
            )
                .execute(pdfUrl)
        } else {
            vm.checkPermission(
                READ_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE,
                requireActivity(),
                requireContext()
            )
            retrievePDFFromDevice(
                currentSavedReport?.savedLocation.toString(),
                currentSavedReport?.DocumentKey.toString()
            )
        }
    }

    private class RetrievePDFFromURL(
        pdfView: PDFView,
        pbPdf: ProgressBar,
        errorLayout: LinearLayout
    ) : AsyncTask<String, Void, InputStream>() {

        private val mypdfView: PDFView = pdfView
        private val pbPdf: ProgressBar = pbPdf
        private val errorLayout: LinearLayout = errorLayout

        // on below line we are calling our do in background method.
        override fun doInBackground(vararg params: String?): InputStream? {
            // on below line we are creating a variable for our input stream.
            var inputStream: InputStream? = null
            try {
                val url = URL(params[0])
                val urlConnection: HttpURLConnection = url.openConnection() as HttpsURLConnection
                if (urlConnection.responseCode == 200) {
                    inputStream = BufferedInputStream(urlConnection.inputStream)
                }
            } catch (e: Exception) {
                pbPdf.isVisible = false
                e.printStackTrace()
                return null
            }
            return inputStream
        }

        override fun onPostExecute(result: InputStream?) {
            if (result == null) {
                errorLayout.isVisible = true
            }
            mypdfView.fromStream(result).load()
            pbPdf.isVisible = false
        }
    }

    private fun retrievePDFFromDevice(filePath: String, documentKey: String) {
        val dbI = SavedReportBuilder.getInstance(requireContext()).SavedReportDao()
        val path = "/storage/emulated/0/Download/$filePath"
        if (File(path).isFile) {
            pdfView.fromFile(
                File(path)
            ).load()
            binding.pbPdf.isVisible = false
        } else {
            binding.pbPdf.isVisible = false
            Toast.makeText(requireContext(), "File Not Found", Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.Default).launch {
                dbI.deleteReport(documentKey)
            }
        }
    }

    private fun shareDocument(url: String) {
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, url)

        if (activity?.packageManager?.resolveActivity(intent, 0) != null) {
            startActivity(intent)
        }
    }
}