package com.ezteam.windowslauncher.widget.pcView.top

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.PopupMenu
import com.ezteam.baseproject.extensions.hideItem
import com.ezteam.baseproject.extensions.showItem
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutPcTopBinding
import com.ezteam.windowslauncher.utils.Config
import com.ezteam.windowslauncher.viewmodel.FileManagerViewModel
import com.ezteam.windowslauncher.widget.pcView.BaseFileManagerView
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Method

class PcViewTop(context: Context, attrs: AttributeSet?) : BaseFileManagerView(context, attrs),
    View.OnClickListener {

    private var binding = LayoutPcTopBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_pc_top, this)
    )

    var closeListener: ((Unit) -> Unit)? = null
    var collapseListener: ((Unit) -> Unit)? = null
    var hideListener: ((Unit) -> Unit)? = null

    init {
        initView()
        initControl()
    }

    private fun initView() {
        hideAllItem()
        binding.tvTopPaste.hideItem()
    }

    private fun initControl() {
        binding.ivCloseFolder.setOnClickListener(this)
        binding.ivHideFolder.setOnClickListener(this)
        binding.ivCollapseFolder.setOnClickListener(this)
        binding.tvTopMore.setOnClickListener(this)
        binding.tvTopNewfolder.setOnClickListener(this)
        binding.tvTopCopy.setOnClickListener(this)
        binding.tvTopCut.setOnClickListener(this)
        binding.tvTopDelete.setOnClickListener(this)
        binding.tvTopPaste.setOnClickListener(this)
        binding.tvTopPinto.setOnClickListener(this)
        binding.tvTopRename.setOnClickListener(this)
        binding.ivUndoFolder.setOnClickListener(this)
        binding.ivRedoFolder.setOnClickListener(this)
        binding.ivUpFolder.setOnClickListener(this)
    }

    private fun setStageUndoRedo() {
        binding.ivUndoFolder.apply {
            viewModel.stackFolderUndo.value?.let {
                isEnabled = !it.isEmpty() || it.size > 1
                alpha = if (it.isEmpty() || it.size <= 1) 0.3f else 1.0f
            } ?: run {
                isEnabled = false
                alpha = 0.3f
            }
        }

        binding.ivRedoFolder.apply {
            viewModel.stackFolderRedo.value?.let {
                isEnabled = !it.isEmpty()
                alpha = if (it.isEmpty()) 0.3f else 1.0f
            } ?: run {
                isEnabled = false
                alpha = 0.3f
            }
        }

        binding.ivUpFolder.apply {
            isEnabled = false
            alpha = 0.3f
            viewModel.getCurrentFolder().let {
                if (!TextUtils.isEmpty(it) && File(it).exists()
                    && !TextUtils.isEmpty(File(it).parent)
                    && it != Config.FileManager.desktopPath(context)
                ) {
                    isEnabled = true
                    alpha = 1.0f
                }
            }
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel.stackFolderUndo.observeForever {
            setStageUndoRedo()
        }
        viewModel.stackFolderUndo.observeForever {
            binding.tvTopMore.visibility = View.VISIBLE
            binding.tvTopNewfolder.visibility = View.VISIBLE
            binding.tvTopCut.visibility = View.VISIBLE
            binding.tvTopPaste.visibility = View.VISIBLE
            when (val folder = it.peek()) {
                Config.FileManager.thisQuickAccessPath -> {
                    binding.fvTitle.setTitle(context.getString(R.string.quick_access))
                    binding.fvTitle.setIcon(R.drawable.ic_start_folder)
                    binding.tvTopNewfolder.visibility = View.GONE
                    binding.tvTopCut.visibility = View.GONE
                    binding.tvTopPaste.visibility = View.GONE
                    binding.tvFolderPath.text = context.getString(R.string.quick_access)
                }
                Config.FileManager.thisFtpPath -> {
                    binding.fvTitle.setTitle(context.getString(R.string.ftp))
                    binding.fvTitle.setIcon(R.drawable.ic_folder_ftp)
                    binding.tvTopMore.visibility = View.GONE
                    binding.tvTopNewfolder.visibility = View.GONE
                    binding.tvFolderPath.text = context.getString(R.string.ftp)
                }
                Config.FileManager.thisLanPath -> {
                    binding.fvTitle.setTitle(context.getString(R.string.lan))
                    binding.fvTitle.setIcon(R.drawable.ic_folder_lan)
                    binding.tvTopMore.visibility = View.GONE
                    binding.tvTopNewfolder.visibility = View.GONE
                    binding.tvFolderPath.text = context.getString(R.string.lan)
                }
                Config.FileManager.thisPCPath -> {
                    binding.fvTitle.setTitle(context.getString(R.string.this_pc))
                    binding.fvTitle.setIcon(R.drawable.ic_computer)
                    binding.tvTopMore.visibility = View.GONE
                    binding.tvTopNewfolder.visibility = View.GONE
                    binding.tvFolderPath.text = context.getString(R.string.this_pc)
                }
                Config.FileManager.thisUserPath -> {
                    binding.fvTitle.setTitle(context.getString(R.string.user))
                    binding.fvTitle.setIcon(R.drawable.ic_computer)
                    binding.tvTopMore.visibility = View.GONE
                    binding.tvTopNewfolder.visibility = View.GONE
                    binding.tvFolderPath.text = context.getString(R.string.user)
                }
                Config.FileManager.externalStoragePath -> {
                    binding.fvTitle.setTitle(context.getString(R.string.local_disk_c2))
                    binding.fvTitle.setIcon(R.drawable.ic_local_disk)
                    binding.tvFolderPath.text = context.getString(R.string.local_disk_c2)
                }
                Config.FileManager.documentPath -> {
                    binding.fvTitle.setTitle(FilenameUtils.getBaseName(folder))
                    binding.fvTitle.setIcon(R.drawable.ic_document_folder_2)
                    binding.tvFolderPath.text = context.getString(R.string.document)
                }
                Config.FileManager.downloadPath -> {
                    binding.fvTitle.setTitle(FilenameUtils.getBaseName(folder))
                    binding.fvTitle.setIcon(R.drawable.ic_download_folder_2)
                    binding.tvFolderPath.text = context.getString(R.string.download)
                }
                Config.FileManager.picturePath -> {
                    binding.fvTitle.setTitle(FilenameUtils.getBaseName(folder))
                    binding.fvTitle.setIcon(R.drawable.ic_picture_folder_2)
                    binding.tvFolderPath.text = context.getString(R.string.picture)
                }
                Config.FileManager.videoPath -> {
                    binding.fvTitle.setTitle(FilenameUtils.getBaseName(folder))
                    binding.fvTitle.setIcon(R.drawable.ic_video_folder)
                    binding.tvFolderPath.text = context.getString(R.string.video)
                }
                Config.FileManager.desktopPath(context) -> {
                    binding.fvTitle.setTitle(context.getString(R.string.desktop))
                    binding.tvTopCut.visibility = View.GONE
                    binding.fvTitle.setIcon(R.drawable.ic_destop_folder)
                    binding.tvFolderPath.text = context.getString(R.string.desktop)
                }
                else -> {
                    binding.fvTitle.setTitle(FilenameUtils.getBaseName(folder))
                    binding.fvTitle.setIcon(R.drawable.ic_folder_default)
                    binding.tvFolderPath.text = it.peek()
                }
            }
            Handler().postDelayed({
                binding.horizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
            }, 100L)
        }

        viewModel.lstFolderSelected.observeForever {
            hideAllItem()
            binding.tvTopPinto.text = context.getString(R.string.pin_access)
            when {
                it.isEmpty() -> {
                    binding.tvTopNewfolder.showItem()
                }
                else -> {
                    if (it.size == 1 && !Config.FileManager.isPrimaryFolder(it[0].path, context))
                        binding.tvTopRename.showItem()
                    binding.tvTopDelete.showItem()
                    binding.tvTopCopy.showItem()
                    binding.tvTopCut.showItem()
                    binding.tvTopPinto.showItem()
                    if (viewModel.getCurrentFolder() == Config.FileManager.thisQuickAccessPath) {
                        binding.tvTopPinto.text = context.getString(R.string.un_pin)
                    }
                }
            }
        }

        viewModel.lstFolderSave.observeForever {
            binding.tvTopPaste.hideItem()
            if (it.isNotEmpty()) {
                binding.tvTopPaste.showItem()
            }
        }
    }

    private fun hideAllItem() {
        binding.tvTopNewfolder.hideItem()
        binding.tvTopRename.hideItem()
        binding.tvTopDelete.hideItem()
        binding.tvTopCopy.hideItem()
        binding.tvTopCut.hideItem()
        binding.tvTopPinto.hideItem()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.iv_close_folder -> {
                closeListener?.invoke(Unit)
            }
            R.id.iv_collapse_folder -> {
                collapseListener?.invoke(Unit)
            }
            R.id.iv_hide_folder -> {
                hideListener?.invoke(Unit)
            }
            R.id.tv_top_more -> {
                showPopupMenuMore(view)
            }
            R.id.tv_top_newfolder -> {
                showCreateFolder()
            }
            R.id.tv_top_rename -> {
                showDilogRenameFile()
            }
            R.id.tv_top_delete -> {
                showDialogDelete()
            }
            R.id.tv_top_paste -> {
                if (viewModel.getCurrentFolder() == Config.FileManager.thisUserPath
                    || viewModel.getCurrentFolder() == Config.FileManager.thisPCPath
                    || viewModel.getCurrentFolder() == Config.FileManager.thisFtpPath
                    || viewModel.getCurrentFolder() == Config.FileManager.thisLanPath
                ) {
                    toast(context.getString(R.string.can_paste_into_folder))
                    return
                }
                viewModel.moveFile(createMultipleFileCallback())
            }
            R.id.tv_top_copy -> {
                viewModel.setStageFile(FileManagerViewModel.StageFile.Copy)
            }
            R.id.tv_top_cut -> {
                viewModel.setStageFile(FileManagerViewModel.StageFile.Cut)
            }
            R.id.tv_top_pinto -> {
                viewModel.pinOrUnpinAccess(viewModel.getCurrentFolder() != Config.FileManager.thisQuickAccessPath)
            }
            R.id.iv_undo_folder -> {
                if (binding.ivUndoFolder.alpha == 1.0f) {
                    viewModel.popDataStack(true)
                    viewModel.cleanStackSelected()
                }
            }
            R.id.iv_redo_folder -> {
                if (binding.ivRedoFolder.alpha == 1.0f) {
                    viewModel.popDataStack(false)
                    viewModel.cleanStackSelected()
                }
            }
            R.id.iv_up_folder -> {
                val parent = File(viewModel.getCurrentFolder()).parentFile
                if (binding.ivUpFolder.alpha == 1.0f) {
                    parent?.let {
                        if (viewModel.getCurrentFolder() == Config.FileManager.externalStoragePath) {
                            viewModel.pushDataUndo(Config.FileManager.thisPCPath)
                        } else {
                            viewModel.pushDataUndo(parent.path)
                        }
                        viewModel.cleanStackSelected()
                    }
                }
            }
        }
    }

    private fun showPopupMenuMore(view: View) {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.menu_folder_more)
        try {
            /*show icon*/
            val fields: Array<Field> = popup.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper: Any = field.get(popup)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons: Method = classPopupHelper.getMethod(
                        "setForceShowIcon",
                        Boolean::class.javaPrimitiveType
                    )
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }

            /*hide navigation bar*/
            @SuppressLint("PrivateApi")
            val wmgClass = Class.forName("android.view.WindowManagerGlobal")
            val wmgInstance = wmgClass.getMethod("getInstance").invoke(null)
            val viewsField = wmgClass.getDeclaredField("mViews")
            viewsField.isAccessible = true
            val views = viewsField.get(wmgInstance) as ArrayList<*>
            views.last().apply {
                systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                setOnSystemUiVisibilityChangeListener {
                    systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (PreferencesUtils.getBoolean(Config.Preferences.folderIsGridView, true)) {
            popup.menu.findItem(R.id.list_view).isVisible = true
            popup.menu.findItem(R.id.grid_view).isVisible = false
        } else {
            popup.menu.findItem(R.id.list_view).isVisible = false
            popup.menu.findItem(R.id.grid_view).isVisible = true
        }

        popup.menu.findItem(R.id.properties).isVisible =
            viewModel.lstFolderSelected.value?.size!! <= 1

        popup.menu.findItem(R.id.select_all).isVisible =
            viewModel.lstFolderSelected.value?.size != viewModel.totalFileInFolder.value?.size

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.select_all -> {
                    viewModel.lstFolderSelected.postValue(viewModel.totalFileInFolder.value)
                }
                R.id.list_view -> {
                    PreferencesUtils.putBoolean(Config.Preferences.folderIsGridView, false)
                    viewModel.folderIsGridView.postValue(false)
                }
                R.id.grid_view -> {
                    PreferencesUtils.putBoolean(Config.Preferences.folderIsGridView, true)
                    viewModel.folderIsGridView.postValue(true)
                }
                R.id.properties -> {
                    var file = File(viewModel.getCurrentFolder())
                    viewModel.lstFolderSelected.value?.let { files ->
                        if (files.isNotEmpty())
                            file = files[0]
                    }
                    if (file.exists())
                        showPropertiesFile(file)
                }
            }
            false
        }
        popup.show()
    }


}