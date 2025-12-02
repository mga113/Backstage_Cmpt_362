package com.group_12.backstage.Chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.group_12.backstage.R
import com.group_12.backstage.UserAccountData.User

class UserAdapter(
    private var userList: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.userNameTextView)
        val userProfileImage: ImageView = itemView.findViewById(R.id.userProfileImage)
        val unreadDot: View = itemView.findViewById(R.id.unreadDot) // Find the dot
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.userName.text = user.name
        holder.itemView.setOnClickListener { onUserClick(user) }

        if (user.profileImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.profileImageUrl)
                .into(holder.userProfileImage)
        } else {
            holder.userProfileImage.setImageResource(R.drawable.ic_person) // Default avatar
        }

        // Show or hide the blue dot based on the 'unread' flag
        holder.unreadDot.visibility = if (user.unread) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = userList.size

    fun updateList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
}
