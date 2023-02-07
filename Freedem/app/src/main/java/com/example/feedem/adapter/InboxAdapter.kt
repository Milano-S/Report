package com.example.feedem.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.feedem.model.ReportData
import com.example.freedem.R
import java.io.File


class InboxAdapter(
    val context: Context,
    val reportList: List<ReportData>,
    val isOnline: Boolean
) : RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    private lateinit var rListener: OnReportItemClickInterface
    private lateinit var saveListener: OnSaveItemClick

    //On Item Click
    interface OnReportItemClickInterface {
        fun onReportItemClick(position: Int)
    }

    fun setOnReportClickListener(listener: OnReportItemClickInterface) {
        rListener = listener
    }

    //Save Pdf Click
    interface OnSaveItemClick {
        fun saveReportPdf(position: Int)
    }

    fun saveReportPdf(listener: OnSaveItemClick) {
        saveListener = listener
    }

    class ViewHolder(
        itemView: View,
        listener: OnReportItemClickInterface,
        btnSave: Button,
        saveListener: OnSaveItemClick
    ) : RecyclerView.ViewHolder(itemView) {

        val tvLocation = itemView.findViewById<TextView>(R.id.tvLocation)
        val tvReportName = itemView.findViewById<TextView>(R.id.tvReportName)
        val tvDateTitle = itemView.findViewById<TextView>(R.id.tvDateTitle)
        val ivSaved = itemView.findViewById<ImageView>(R.id.ivSaved)
        val isRead = itemView.findViewById<ConstraintLayout>(R.id.isRead)

        init {
            itemView.setOnClickListener {
                isRead.setBackgroundColor(Color.WHITE)
                listener.onReportItemClick(layoutPosition)
            }
            btnSave.setOnClickListener {
                saveListener.saveReportPdf(layoutPosition)
                btnSave.isClickable = false
                btnSave.isVisible = false
                btnSave.isEnabled = false
                ivSaved.isVisible = true
            }
        }

        fun bindInbox(report: ReportData) {
            val isRead = itemView.findViewById<ConstraintLayout>(R.id.isRead)
            val btnSave = itemView.findViewById<Button>(R.id.btnSave)
            if (report.isRead) {
                isRead.setBackgroundColor(Color.WHITE)
            }
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
        val itemView = LayoutInflater.from(context).inflate(R.layout.document_card, parent, false)
        return ViewHolder(itemView, rListener, itemView.findViewById(R.id.btnSave), saveListener)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.isRead.setBackgroundColor(ContextCompat.getColor(context, R.color.feedemPink))
        val btnSave = holder.itemView.findViewById<Button>(R.id.btnSave)
        val ivSaved = holder.itemView.findViewById<ImageView>(R.id.ivSaved)
        btnSave.isVisible = true
        btnSave.isEnabled = true
        ivSaved.isVisible = false
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val inboxItem = reportList[holder.adapterPosition]
        holder.bindInbox(inboxItem)
    }

    override fun getItemCount() = reportList.size
}