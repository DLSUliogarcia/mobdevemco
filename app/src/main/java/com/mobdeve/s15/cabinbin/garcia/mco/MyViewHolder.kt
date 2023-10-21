package com.mobdeve.s15.cabinbin.garcia.mco

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class MyViewHolder (itemView: View): ViewHolder(itemView){
    private val rv: TextView = itemView.findViewById(R.id.rankTv)
    private val sv: TextView = itemView.findViewById(R.id.scoreTv)
    private val dv: TextView = itemView.findViewById(R.id.dateTv)

    fun bindData(score: Score){
        dv.text = score.dateAchieved
        rv.text = score.rank.toString()
        sv.text = score.numScore.toString()
    }

}