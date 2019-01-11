package com.gopi.googlepay.adapter

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gopi.googlepay.GlideApp

import com.gopi.googlepay.R
import com.gopi.googlepay.activity.MainActivity
import com.gopi.googlepay.model.ItemInfo

class ItemAdapter(private val itemList: List<ItemInfo>, val context: MainActivity) : RecyclerView.Adapter<ItemAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: AppCompatTextView = view.findViewById(R.id.txtItemName)
        val price: AppCompatTextView? = view.findViewById(R.id.txtItemPrice)
        val image: AppCompatImageView? = view.findViewById(R.id.imgItem)
        val gPay: AppCompatImageView = view.findViewById(R.id.imgGpay)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.raw_item, parent, false)

        return MyViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemList[position]
        holder.price?.text = item.priceMicros.toString()
        holder.title.text = item.name
        GlideApp.with(context)
                .load(item.imageResourceId)
                .into(holder.image)
        holder.gPay.setOnClickListener {
            if (context.isGPayAvailable) {
                context.requestPayment(item)
            } else {
                Toast.makeText(context, "Unfortunately, Google Pay is not available on this phone.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
