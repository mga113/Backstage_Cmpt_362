package com.group_12.backstage_group_12.MyInterests

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group_12.backstage_group_12.R

class MyInterestsAdapter(private val events: MutableList<Event>) :
    RecyclerView.Adapter<MyInterestsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventImage: ImageView = itemView.findViewById(R.id.eventImage)
        val eventName: TextView = itemView.findViewById(R.id.eventName)
        val eventVenue: TextView = itemView.findViewById(R.id.eventVenue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]
        holder.eventName.text = event.title
        holder.eventVenue.text = event.location
        holder.eventImage.setImageResource(R.drawable.sebastian_unsplash)
    }

    override fun getItemCount(): Int = events.size
}