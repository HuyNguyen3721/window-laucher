package com.ezteam.windowslauncher.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutItemControlBinding
import com.ezteam.windowslauncher.model.ItemControl

class AdapterControl(private val listDataControl: MutableList<ItemControl>) :
    RecyclerView.Adapter<AdapterControl.ViewHolder>() {
    var listenerClickItem: ((String, Int) -> Unit)? = null
    var listenerLongClickItem: ((String) -> Unit)? = null

    inner class ViewHolder(var binding: LayoutItemControlBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutItemControlBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animClickIcon =
            AnimationUtils.loadAnimation(holder.binding.root.context, R.anim.anim_click_icon)
        val data = listDataControl[position]
        holder.binding.imgIcon.setImageResource(data.resId)
        holder.binding.txtNameIcon.text = data.name
        holder.itemView.setOnClickListener {
            holder.binding.root.startAnimation(animClickIcon)
            listenerClickItem?.invoke(
                listDataControl[holder.adapterPosition].name,
                holder.adapterPosition
            )
        }
        holder.itemView.setOnLongClickListener {
            holder.binding.root.startAnimation(animClickIcon)
            listenerLongClickItem?.invoke(listDataControl[holder.adapterPosition].name)
            true
        }
        if (data.isEnable || data.isClickEnable) {
            holder.binding.layoutItemControl.setBackgroundColor(
                holder.binding.root.context.resources.getColor(
                    R.color.color_0094FF
                )
            )
        } else {
            holder.binding.layoutItemControl.setBackgroundColor(
                holder.binding.root.context.resources.getColor(
                    R.color.color_1AFFFFFF
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return listDataControl.size
    }
}