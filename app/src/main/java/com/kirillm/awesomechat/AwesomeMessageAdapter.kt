package com.kirillm.awesomechat

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class AwesomeMessageAdapter(private val context: Activity,
                            resource: Int,
                            private val objects: MutableList<AwesomeMessage>) :
    ArrayAdapter<AwesomeMessage>(context, resource, objects) {

    //var activity = context

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View
        val viewHolder: AwesomeMessageViewHolder
        val layoutInflater =
            context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val awesomeMessage = getItem(position)
        val viewType = getItemViewType(position)

        val layoutResource = if(viewType == 0)
            R.layout.my_message_item
        else
            R.layout.your_message_item

        if(convertView != null){
            viewHolder = convertView.tag as AwesomeMessageViewHolder
            view = convertView
        }
        else{
            view = layoutInflater
                .inflate(layoutResource, parent, false)
            viewHolder = AwesomeMessageViewHolder(view)
            view.tag = viewHolder
        }

        if (awesomeMessage != null) {
            if (awesomeMessage.imageUrl.isEmpty()) {
                viewHolder.messageTextView.visibility = View.VISIBLE
                viewHolder.messageTextView.text = awesomeMessage.text
                viewHolder.photoImageView.visibility = View.GONE
            } else {
                viewHolder.messageTextView.visibility = View.GONE
                viewHolder.photoImageView.visibility = View.VISIBLE
                Glide.with(viewHolder.photoImageView.context)
                    .load(awesomeMessage.imageUrl)
                    .into(viewHolder.photoImageView)
            }
        }

        return view
    }

    override fun getItemViewType(position: Int): Int {
        val awesomeMessage = objects[position]
        return if (awesomeMessage.isMine)
            0
        else
            1
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    private class AwesomeMessageViewHolder(view: View) {
       var photoImageView: ImageView = view.findViewById(R.id.photoImageView)
       var messageTextView: TextView = view.findViewById(R.id.messageTextView)
    }
}