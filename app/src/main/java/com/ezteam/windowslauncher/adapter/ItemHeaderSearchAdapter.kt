package com.ezteam.windowslauncher.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.ItemHeaderSearchBinding

class ItemHeaderSearchAdapter(
    context: Context,
    list: MutableList<String>,
    var headersEnable: List<String>,
    var itemOnClickListener: (String) -> Unit
) : BaseRecyclerAdapter<String, ItemHeaderSearchAdapter.ViewHolder>(context, list) {
    inner class ViewHolder(var binding: ItemHeaderSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(header: String) {
            binding.tvHeader.text = header
            headersEnable.find {
                it.trim() == header.trim() || header == "#"
            }?.let {
                binding.root.isEnabled = true
                binding.tvHeader.setTextColor(mContext.resources.getColor(R.color.tokenWhite100))
            } ?: run {
                binding.root.isEnabled = false
                binding.tvHeader.setTextColor(mContext.resources.getColor(R.color.tokenWhite40))
            }

            binding.root.setOnClickListener {
                itemOnClickListener(header)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHeaderSearchBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
}