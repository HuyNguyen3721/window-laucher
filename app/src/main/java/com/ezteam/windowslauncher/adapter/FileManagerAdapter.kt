package com.ezteam.windowslauncher.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.ItemManagerFolderHorizontalBinding
import com.ezteam.windowslauncher.databinding.ItemManagerFolderVerticalBinding
import com.ezteam.windowslauncher.utils.Config
import com.ezteam.windowslauncher.utils.FileUtils
import com.ezteam.windowslauncher.widget.FolderView
import org.apache.commons.io.FilenameUtils
import java.io.File

class FileManagerAdapter(context: Context?, list: MutableList<File>) :
    BaseRecyclerAdapter<File, RecyclerView.ViewHolder>(
        context, list as List<File>?
    ) {

    var itemClickListener: ((File) -> Unit)? = null
    var itemLongClickListener: ((File) -> Unit)? = null
    var stackItemSelected = mutableListOf<File>()
    var isHorizoltal = true

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isHorizoltal) {
            (holder as ViewHolderHorizontal).binData(list[position])
        } else {
            (holder as ViewHolderVertical).binData(list[position])
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        if (isHorizoltal) {
            return ViewHolderHorizontal(
                layoutInflater.inflate(
                    R.layout.item_manager_folder_horizontal,
                    parent,
                    false
                )
            )
        } else {
            return ViewHolderVertical(
                layoutInflater.inflate(
                    R.layout.item_manager_folder_vertical,
                    parent,
                    false
                )
            )
        }
    }

    private fun initView(
        file: File,
        adapterPosition: Int,
        itemView: View,
        folderView: FolderView,
        checkBox: CheckBox
    ) {
        file.let {
            folderView.setTitle(
                FilenameUtils.getBaseName(
                    it.path
                )
            )
            if (it.isDirectory) {
                when (it.path) {
                    Config.FileManager.picturePath -> folderView.setIcon(R.drawable.ic_picture_folder)
                    Config.FileManager.documentPath -> folderView.setIcon(R.drawable.ic_document_folder)
                    Config.FileManager.downloadPath -> folderView.setIcon(R.drawable.ic_download_folder)
                    Config.FileManager.videoPath -> folderView.setIcon(R.drawable.ic_video_folder)
                    Config.FileManager.desktopPath(mContext) -> folderView.setIcon(R.drawable.ic_destop_folder)
                    else -> folderView.setIcon(R.drawable.ic_folder_default)
                }
            } else {
                if (FileUtils.isFileImage(it.path)) {
                    folderView.setIcon(it.path, R.drawable.ic_image_defaulf_folder)
                } else {
                    folderView.setIcon(FileUtils.getIconResId(it.name))
                }
            }

            itemView.setBackgroundColor(0)
            checkBox.visibility = View.GONE
            stackItemSelected.let { stack ->
                if (stack.contains(file)) {
                    itemView.setBackgroundColor(mContext.resources.getColor(R.color.color_def2f9))
                    checkBox.visibility = View.VISIBLE
                }
            }

            itemView.setOnClickListener {
                itemClickListener?.invoke(file)
                notifyItemChanged(adapterPosition)
            }

            itemView.setOnLongClickListener {
                itemLongClickListener?.invoke(file)
                notifyItemChanged(adapterPosition)
                true
            }
        }
    }

    inner class ViewHolderHorizontal(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding = ItemManagerFolderHorizontalBinding.bind(itemView)

        fun binData(file: File) {
            initView(file, adapterPosition, itemView, binding.fvItem, binding.cbSelected)
        }
    }

    inner class ViewHolderVertical(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding = ItemManagerFolderVerticalBinding.bind(itemView)

        fun binData(file: File) {
            initView(file, adapterPosition, itemView, binding.fvItem, binding.cbSelected)
        }
    }


}