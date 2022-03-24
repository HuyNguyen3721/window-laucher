package com.ezteam.windowslauncher.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ezteam.baseproject.viewmodel.BaseViewModel
import com.ezteam.windowslauncher.utils.Config
import com.kwabenaberko.newsapilib.NewsApiClient
import com.kwabenaberko.newsapilib.models.Article
import com.kwabenaberko.newsapilib.models.request.EverythingRequest
import com.kwabenaberko.newsapilib.models.response.ArticleResponse

class NewspaperViewModel(application: Application) : BaseViewModel(application) {
    fun getNewspaperStories(): MutableLiveData<MutableList<Article>> {
        val newspaperLiveData: MutableLiveData<MutableList<Article>> =
            MutableLiveData(mutableListOf())
        val apiClient = NewsApiClient(Config.NEWSPAPER_API_KEY)
        apiClient.getEverything(
            EverythingRequest.Builder()
                .q("world")
                .build(),
            object : NewsApiClient.ArticlesResponseCallback {
                override fun onSuccess(response: ArticleResponse) {
                    newspaperLiveData.value = response.articles.sortedByDescending {
                        it.publishedAt
                    } as MutableList<Article>
                    println(response.articles[0].title)
                }

                override fun onFailure(throwable: Throwable) {
                    println(throwable.message)
                }
            }
        )
        return newspaperLiveData
    }

    fun getNewspaperCollections(): MutableLiveData<MutableList<Article>> {
        val newspaperLiveData: MutableLiveData<MutableList<Article>> =
            MutableLiveData(mutableListOf())
        val apiClient = NewsApiClient(Config.NEWSPAPER_API_KEY)
        apiClient.getEverything(
            EverythingRequest.Builder()
                .q("world")
                .build(),
            object : NewsApiClient.ArticlesResponseCallback {
                override fun onSuccess(response: ArticleResponse) {
                    val articles = response.articles.sortedByDescending {
                        it.publishedAt
                    } as MutableList<Article>

                    newspaperLiveData.value =
                        if (articles.size > 8) articles.subList(0, 8) else articles
                    println(response.articles[0].title)
                }

                override fun onFailure(throwable: Throwable) {
                    println(throwable.message)
                }
            }
        )
        return newspaperLiveData
    }
}