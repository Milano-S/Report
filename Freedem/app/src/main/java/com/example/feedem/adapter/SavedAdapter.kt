package com.example.feedem.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.feedem.model.SavedReportData
import com.example.freedem.R

class SavedAdapter(
    val context: Context,
    val savedList: List<SavedReportData>
) : RecyclerView.Adapter<SavedAdapter.ViewHolder>() {

    private lateinit var siListener: OnSaveReportClick

    //Item Click
    interface OnSaveReportClick { fun onSaveItemClick(position: Int) }
    fun setOnSaveReportItemListener(listener: OnSaveReportClick){
        siListener = listener
    }


    inner class ViewHolder(
        itemView: View,
        listener: OnSaveReportClick
    ): RecyclerView.ViewHolder(itemView){
        fun bind(savedReport: SavedReportData){
            val tvLocation = itemView.findViewById<TextView>(R.id.tvLocation)
            val tvReportName = itemView.findViewById<TextView>(R.id.tvReportName)
            val tvDateTitle = itemView.findViewById<TextView>(R.id.tvDateTitle)
            val btnSave = itemView.findViewById<Button>(R.id.btnSave)

            tvLocation.text = savedReport.BranchName
            tvReportName.text = savedReport.ReportName
            tvDateTitle.text = savedReport.DateTitle
            btnSave.isEnabled = false
            btnSave.isClickable = false

            itemView.setOnClickListener {
                siListener.onSaveItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.document_card_save, parent, false)
        return ViewHolder(view, siListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(savedList[position])
    }

    override fun getItemCount() = savedList.size
}