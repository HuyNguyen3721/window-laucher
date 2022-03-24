package com.ezteam.windowslauncher.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.windowslauncher.databinding.ItemNewspaperCollectionBinding
import com.kwabenaberko.newsapilib.models.Article

class ItemNewspaperCollectionAdapter(
    context: Context,
    list: List<Article>,
    var itemClickListener: (Article) -> Unit
) : BaseRecyclerAdapter<Article, ItemNewspaperCollectionAdapter.ViewHolder>(context, list) {
    inner class ViewHolder(var binding: ItemNewspaperCollectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(article: Article) {
            val sourceName = article.source.name
            binding.tvTitle.text = if (sourceName.isNotEmpty()) sourceName else article.author
            Glide.with(mContext)
                .load(article.urlToImage)
                .into(binding.ivNews)

            binding.root.setOnClickListener {
                itemClickListener(article)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNewspaperCollectionBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
}