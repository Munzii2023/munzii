package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemRetrofitBinding
import retrofit2.Callback

class MyRetrofitViewHolder(val binding: ItemRetrofitBinding): RecyclerView.ViewHolder(binding.root)

class RetrofitAdapter(val context: Callback<MyModel>, val datas: MutableList<MyItem>?): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    var firstStation:String ?= null

    override fun getItemCount(): Int{
        return datas?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = MyRetrofitViewHolder(ItemRetrofitBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as MyRetrofitViewHolder).binding

        //add......................................
        val model = datas!![position]
        binding.itemTm.text = model.item.tm
        binding.itemAddr.text = model.item.addr
        binding.itemStationName.text = model.item.stationName


        firstStation = datas[0].item.stationName
    }
    fun getStation() : String? {
        return firstStation
    }
}