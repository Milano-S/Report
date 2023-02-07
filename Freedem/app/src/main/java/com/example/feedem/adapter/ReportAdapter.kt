package com.exclr8.n1reportmanagement.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.feedem.model.ReportData
import com.example.freedem.R
import java.io.File

class ReportAdapter(
    val context: Context,
    val reportList: List<ReportData>,
    val isOnline: Boolean
) : RecyclerView.Adapter<ReportAdapter.ViewHolder>() {

    private lateinit var rListener: onReportItemClickInterface
    private lateinit var saveListener: OnSaveItemClick

    //On Item Click
    interface onReportItemClickInterface {
        fun onReportItemClick(position: Int)
    }

    fun setOnReportClickListener(listener: onReportItemClickInterface) {
        rListener = listener
    }

    //Save Pdf Click
    interface OnSaveItemClick {
        fun saveReportPdf(position: Int)
    }

    fun saveReportPdf(listener: OnSaveItemClick) {
        saveListener = listener
    }

    inner class ViewHolder(
        itemView: View,
        listener: onReportItemClickInterface,
        btnSave: Button,
        saveListener: OnSaveItemClick
    ) : RecyclerView.ViewHolder(itemView) {

        var tvLocation: TextView
        var tvReportName: TextView
        var tvDateTitle: TextView
        var ivSaved: ImageView

        init {
            tvLocation = itemView.findViewById(R.id.tvLocation)
            tvReportName = itemView.findViewById(R.id.tvReportName)
            tvDateTitle = itemView.findViewById(R.id.tvDateTitle)
            ivSaved = itemView.findViewById(R.id.ivSaved)

            itemView.setOnClickListener {
                listener.onReportItemClick(adapterPosition)
            }
            btnSave.setOnClickListener {
                saveListener.saveReportPdf(position)
                btnSave.isClickable = false
                btnSave.isVisible = false
                btnSave.isEnabled = false
                ivSaved.isVisible = true
            }
        }

        fun bindReports(report: ReportData){
            val btnSave = itemView.findViewById<Button>(R.id.btnSave)
            if (File("/storage/emulated/0/Download/" + report.DateTitle + "-" + report.DocumentKey).isFile){
                btnSave.isEnabled = false
                btnSave.isVisible = false
                ivSaved.isVisible = true
            }
            tvLocation.text = report.BranchName
            tvReportName.text = report.ReportName
            tvDateTitle.text = report.DateTitle
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.document_card_report, parent, false)
        return ViewHolder(itemView, rListener, itemView.findViewById(R.id.btnSave), saveListener)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        val btnSave = holder.itemView.findViewById<Button>(R.id.btnSave)
        val ivSaved = holder.itemView.findViewById<ImageView>(R.id.ivSaved)
        btnSave.isVisible = true
        btnSave.isEnabled = true
        ivSaved.isVisible = false
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reportItem = reportList[holder.adapterPosition]
        holder.bindReports(reportItem)
    }
    override fun getItemCount() = reportList.size
}