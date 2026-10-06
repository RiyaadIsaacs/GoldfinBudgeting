package com.example.goldfinbudgeting

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    //hold one message bubble view
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
    }

    //inflate bubble
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    //fill bubble with info
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {

        val message = messages[position]

        holder.messageText.text = message.text

        val params = holder.messageText.layoutParams as FrameLayout.LayoutParams

        if (message.isFromUser) {
            //make user messages appear on the right in purple bubble
            params.gravity = Gravity.END

            holder.messageText.setBackgroundResource(R.drawable.rounded_bubble_sent)

        } else {
            //make assistant messages appear on left side in white bubble
            params.gravity = Gravity.START

            holder.messageText.setBackgroundResource(R.drawable.rounded_bubble_received)
        }

        holder.messageText.layoutParams = params
    }

    //detemines how many messages to show
    override fun getItemCount(): Int {

        return messages.size
    }
}