package com.group_12.backstage.ConcertConnect

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.group_12.backstage.R
import com.group_12.backstage.UserAccountData.User

class FindFriendsAdapter(
    private val onAddClick: (User) -> Unit,
    private val onDeclineClick: (User) -> Unit, // Only for received requests
    private val onAcceptClick: (User) -> Unit   // Reusing Add button for Accept
) : RecyclerView.Adapter<FindFriendsAdapter.UserViewHolder>() {

    private var users = listOf<User>()
    private var friendStatuses = mapOf<String, String>() // "uid" -> "none" | "friend" | "sent" | "received"

    fun submitList(newUsers: List<User>, newStatuses: Map<String, String>) {
        users = newUsers
        friendStatuses = newStatuses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_find_friends_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        val status = friendStatuses[user.uid] ?: "none"
        holder.bind(user, status)
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ShapeableImageView = itemView.findViewById(R.id.userProfileImage)
        private val name: TextView = itemView.findViewById(R.id.userName)
        private val email: TextView = itemView.findViewById(R.id.userEmail)
        private val actionBtn: Button = itemView.findViewById(R.id.actionButton)
        private val declineBtn: ImageButton = itemView.findViewById(R.id.declineButton)

        fun bind(user: User, status: String) {
            name.text = user.name.ifEmpty { "Unknown" }
            email.text = user.email

            if (user.profileImageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.ic_account_circle)
                    .error(R.drawable.ic_account_circle)
                    .centerCrop()
                    .into(image)
            } else {
                image.setImageResource(R.drawable.ic_account_circle)
            }

            // Reset state
            declineBtn.isVisible = false
            actionBtn.isEnabled = true
            actionBtn.setTextColor(Color.WHITE)

            when (status) {
                "friend" -> {
                    actionBtn.text = "Friends"
                    actionBtn.setBackgroundColor(Color.GRAY)
                    actionBtn.isEnabled = false
                    actionBtn.setOnClickListener(null)
                }
                "sent" -> {
                    actionBtn.text = "Sent"
                    actionBtn.setBackgroundColor(Color.LTGRAY)
                    actionBtn.isEnabled = false
                    actionBtn.setOnClickListener(null)
                }
                "received" -> {
                    actionBtn.text = "Accept"
                    actionBtn.setBackgroundColor(Color.parseColor("#00CED1")) // Tropical Teal
                    actionBtn.setOnClickListener { onAcceptClick(user) }
                    
                    declineBtn.isVisible = true
                    declineBtn.setOnClickListener { onDeclineClick(user) }
                }
                else -> { // "none"
                    actionBtn.text = "Add"
                    actionBtn.setBackgroundColor(Color.parseColor("#00CED1")) // Tropical Teal
                    actionBtn.setOnClickListener { onAddClick(user) }
                }
            }
        }
    }
}
