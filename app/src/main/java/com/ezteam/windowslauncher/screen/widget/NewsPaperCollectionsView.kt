package com.ezteam.windowslauncher.screen.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezteam.baseproject.extensions.lifecycleOwner
import com.ezteam.baseproject.view.ItemDecorationAlbumColumns
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.ItemNewspaperCollectionAdapter
import com.ezteam.windowslauncher.databinding.LayoutNewspaperCollectionsBinding
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import com.ezteam.windowslauncher.viewmodel.NewspaperViewModel
import com.kwabenaberko.newsapilib.models.Article
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class NewsPaperCollectionsView(
    context: Context, attrs: AttributeSet?
) : ConstraintLayout(context, attrs), KoinComponent {
    private val binding = LayoutNewspaperCollectionsBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_newspaper_collections, this)
    )
    private lateinit var adapter: ItemNewspaperCollectionAdapter
    private val viewModel by inject<NewspaperViewModel>()
    private val mainViewModel by inject<MainViewModel>()
    private var decorView: ItemDecorationAlbumColumns? = null

    init {
        initViews()
        initData()
    }

    private fun initViews() {
        adapter = ItemNewspaperCollectionAdapter(context, mutableListOf(), this::onItemClick)
        binding.rcvNewspaper.adapter = adapter


    }

    private fun initData() {
        context.lifecycleOwner()?.let {
            viewModel.getNewspaperCollections().observe(it) {
                adapter.setList(it)
                adapter.notifyDataSetChanged()
            }
        }

        context.lifecycleOwner()?.let {
            mainViewModel.orientationLiveData.observe(it) {
                when (it) {
                    Configuration.ORIENTATION_PORTRAIT -> {
                        decorView?.let {
                            binding.rcvNewspaper.removeItemDecoration(it)
                        }

                        decorView = ItemDecorationAlbumColumns(
                            resources.getDimensionPixelSize(R.dimen._10sdp),
                            2
                        )
                        binding.rcvNewspaper.addItemDecoration(decorView!!)
                    }

                    Configuration.ORIENTATION_LANDSCAPE -> {
                        decorView?.let {
                            binding.rcvNewspaper.removeItemDecoration(it)
                        }
                        decorView = ItemDecorationAlbumColumns(
                            resources.getDimensionPixelSize(R.dimen._10sdp),
                            4
                        )
                        binding.rcvNewspaper.addItemDecoration(decorView!!)
                    }
                }
            }
        }
    }

    private fun onItemClick(article: Article) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(article.url)
        context.startActivity(intent)
    }
}