package com.ezteam.windowslauncher.screen.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezteam.baseproject.extensions.lifecycleOwner
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.ItemNewspaperStoriesAdapter
import com.ezteam.windowslauncher.databinding.LayoutNewspaperStoriesBinding
import com.ezteam.windowslauncher.viewmodel.NewspaperViewModel
import com.kwabenaberko.newsapilib.models.Article
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class NewsPaperStoriesView(
    context: Context, attrs: AttributeSet?
) : ConstraintLayout(context, attrs), KoinComponent {
    private val binding = LayoutNewspaperStoriesBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_newspaper_stories, this)
    )
    private lateinit var adapter: ItemNewspaperStoriesAdapter
    private val viewModel by inject<NewspaperViewModel>()

    init {
        initViews()
        initData()
    }

    private fun initViews() {
        adapter = ItemNewspaperStoriesAdapter(context, mutableListOf(), this::onItemClick)
        binding.rcvNewspaper.adapter = adapter
    }

    private fun initData() {
        context.lifecycleOwner()?.let {
            viewModel.getNewspaperStories().observe(it) {
                adapter.setList(it)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun onItemClick(article: Article) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(article.url)
        context.startActivity(intent)
    }
}