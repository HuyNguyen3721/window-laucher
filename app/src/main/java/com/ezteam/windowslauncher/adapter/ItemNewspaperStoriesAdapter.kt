package com.ezteam.windowslauncher.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.baseproject.extensions.timeLast
import com.ezteam.baseproject.utils.DateUtils
import com.ezteam.windowslauncher.databinding.ItemNewspaperStoriesBinding
import com.kwabenaberko.newsapilib.models.Article
import java.util.*

class ItemNewspaperStoriesAdapter(
    context: Context,
    list: List<Article>,
    var itemClickListener: (Article) -> Unit
) : BaseRecyclerAdapter<Article, ItemNewspaperStoriesAdapter.ViewHolder>(context, list) {
    inner class ViewHolder(var binding: ItemNewspaperStoriesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(article: Article) {
            val sourceName = article.source.name
            binding.tvSource.text = if (sourceName.isNotEmpty()) sourceName else article.author
            binding.tvDescription.text = article.description

            val date: Date? =
                DateUtils.convertStringToDate(article.publishedAt, "yyyy-MM-dd'T'HH:mm:ssXXX")
            binding.tvTimeUp.text = date?.timeLast()

            binding.root.setOnClickListener {
                itemClickListener(article)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNewspaperStoriesBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
}