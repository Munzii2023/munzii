package com.example.myapplication

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RetrofitAdapter (var items : Array<ModelMunzzi>) : RecyclerView.Adapter<RetrofitAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RetrofitAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_retrofit, parent, false)
        return ViewHolder(itemView)
    }

    // 전달받은 위치의 아이템 연결
    override fun onBindViewHolder(holder: RetrofitAdapter.ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    // 아이템 갯수 리턴
    override fun getItemCount() = items.count()

    // 뷰 홀더 설정
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun setItem(item : ModelMunzzi) {
            val tvTime = itemView.findViewById<TextView>(R.id.item_name)           // 시각
            // tvHumidity = itemView.findViewById<TextView>(R.id.tvHumidity)   // 습도
            //val tvTemp = itemView.findViewById<TextView>(R.id.tvTemp)           // 온도

            tvTime.text = item.pm25Value
            //tvHumidity.text = item.humidity +"%"
            //tvTemp.text = item.temp + "°"
        }
    }

}