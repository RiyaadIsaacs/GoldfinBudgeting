package com.example.goldfinbudgeting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EnvelopeAdapter(private val envelopes: List<Envelope>) :
    RecyclerView.Adapter<EnvelopeAdapter.EnvelopeViewHolder>() {

    //contains one card views in scene so don't have to look them up every time
    class EnvelopeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.nameText)
        val minMaxText: TextView = itemView.findViewById(R.id.minMaxText)
        val spentText: TextView = itemView.findViewById(R.id.spentText)
        val fillBar: View = itemView.findViewById(R.id.fillBar)
        val fillSpacer: View = itemView.findViewById(R.id.fillSpacer)
        val markerBefore: View = itemView.findViewById(R.id.markerBefore)
        val markerAfter: View = itemView.findViewById(R.id.markerAfter)
    }

    //inflate one card
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnvelopeViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_envelope, parent, false)

        return EnvelopeViewHolder(view)
    }

    //fill one card with data
    override fun onBindViewHolder(holder: EnvelopeViewHolder, position: Int) {

        val envelope = envelopes[position]

        holder.nameText.text = envelope.name
        holder.minMaxText.text = "${envelope.min.toInt()} Min / ${envelope.max.toInt()} Max"
        holder.spentText.text = "Spent: ${envelope.spent.toInt()}"

        //working out fill and marker percentages
        val fillPercent = ((envelope.spent / envelope.max) * 100).coerceAtMost(100.0)
        val markerPercent = (envelope.min / envelope.max) * 100

        val fillParams = holder.fillBar.layoutParams as LinearLayout.LayoutParams

        fillParams.weight = fillPercent.toFloat()

        holder.fillBar.layoutParams = fillParams

        val spacerParams = holder.fillSpacer.layoutParams as LinearLayout.LayoutParams

        spacerParams.weight = (100 - fillPercent).toFloat()

        holder.fillSpacer.layoutParams = spacerParams

        val beforeParams = holder.markerBefore.layoutParams as LinearLayout.LayoutParams

        beforeParams.weight = markerPercent.toFloat()

        holder.markerBefore.layoutParams = beforeParams

        val afterParams = holder.markerAfter.layoutParams as LinearLayout.LayoutParams

        afterParams.weight = (100 - markerPercent).toFloat()

        holder.markerAfter.layoutParams = afterParams

        //red fill when percentage reaches max
        if (envelope.spent >= envelope.max) {

            holder.fillBar.setBackgroundResource(R.drawable.rounded_fill_red)

        } else {

            holder.fillBar.setBackgroundResource(R.drawable.rounded_fill_gold)
        }
    }

    //numbr of cards to show in view
    override fun getItemCount(): Int {

        return envelopes.size
    }
}