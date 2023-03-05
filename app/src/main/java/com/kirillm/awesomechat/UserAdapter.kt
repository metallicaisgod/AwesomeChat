package com.kirillm.awesomechat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UserAdapter(private val users: ArrayList<User>): RecyclerView.Adapter<UserViewHolder>() {

    lateinit var clickListener: OnClickUserListener

    interface OnClickUserListener{
        fun onUserClick(position: Int)
    }

    fun setOnClickUserListener(listener: OnClickUserListener){
        clickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_card, parent, false)
        return UserViewHolder(itemView, clickListener)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        Glide.with(holder.avatarImageView.context)
            .load(users[position].avatarResource)
            .circleCrop()
            .into(holder.avatarImageView)
        holder.userNameTextView.text = users[position].name
    }
}

class UserViewHolder(itemView: View, listener: UserAdapter.OnClickUserListener?) : RecyclerView.ViewHolder(itemView) {
    val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
    val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
    init {
        itemView.setOnClickListener {
            if(listener != null){
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onUserClick(position)
                }
            }
        }
    }

}
