package com.ezteam.windowslauncher.widget.pcView.center

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.HorizontalScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezteam.baseproject.extensions.openFile
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.FileManagerAdapter
import com.ezteam.windowslauncher.databinding.LayoutPcCenterRightBinding
import com.ezteam.windowslauncher.utils.Config
import com.ezteam.windowslauncher.utils.FileUtils
import com.ezteam.windowslauncher.utils.InternetHelper
import com.ezteam.windowslauncher.utils.Utils
import com.ezteam.windowslauncher.utils.swiftp.BroadcastDisconnectFtp
import com.ezteam.windowslauncher.utils.swiftp.FTPServerService
import com.ezteam.windowslauncher.viewmodel.FileManagerViewModel
import com.ezteam.windowslauncher.widget.GridAutofitLayoutManager
import com.ezteam.windowslauncher.widget.pcView.BaseFileManagerView
import com.google.android.gms.ads.ez.EzAdControl
import java.io.File


class PcViewCenterRight(context: Context, attrs: AttributeSet?) :
    BaseFileManagerView(context, attrs),
    View.OnClickListener {

    private val bindingRight = LayoutPcCenterRightBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_pc_center_right, this)
    )

    private val broadCastListener = BroadcastDisconnectFtp()
    private lateinit var fileManagerAdapter: FileManagerAdapter
    private val listPosition = Bundle()
    private var stageSelected = false

    init {
        initView()
        initControl()
        viewModel.pushDataUndo(Config.FileManager.thisPCPath)
    }

    private fun initView() {
        fileManagerAdapter = FileManagerAdapter(context, mutableListOf())
        bindingRight.rcvFolder.adapter = fileManagerAdapter

        val totalMemory = Utils.getTotalMemoryStorage()
        val availableMemory = Utils.getMemoryStorageAvailable()
        val userMemory = totalMemory - availableMemory
        bindingRight.prgMemory.progress =
            (userMemory.toDouble() / totalMemory.toDouble() * 100).toInt()

        bindingRight.tvStatusMemory.text =
            context.getString(
                R.string.status_memory,
                FileUtils.formatSize(availableMemory),
                FileUtils.formatSize(totalMemory)
            )
        setStageFtp(FTPServerService.isRunning())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel.expendLeftLayout.observeForever {
            initAdapter()
        }
        viewModel.stackFolderUndo.observeForever {
            if (!it.empty()) {
                val path = it.peek()
                updateAdapter(path)
            }
        }
        viewModel.folderIsGridView.observeForever {
            initAdapter()
        }
        viewModel.lstFolderSelected.observeForever {
            fileManagerAdapter.stackItemSelected = it
            if (it.isEmpty()) {
                stageSelected = false
                fileManagerAdapter.notifyDataSetChanged()
            } else if (it.size == fileManagerAdapter.list.size) {
                fileManagerAdapter.notifyDataSetChanged()
            }
        }
        viewModel.lstFolderSave.observeForever {
            if (it.isEmpty() && viewModel.stageFileSave == FileManagerViewModel.StageFile.None) {
                updateAdapter(viewModel.getCurrentFolder())
            }
        }
    }

    private fun initAdapter() {
        val layoutManager = if (viewModel.folderIsGridView.value == true) {
            GridAutofitLayoutManager(
                context,
                resources.getDimensionPixelSize(R.dimen._80sdp),
                GridLayoutManager.VERTICAL,
                false
            )
        } else {
            LinearLayoutManager(context, GridLayoutManager.VERTICAL, false)
        }
        val arrData = mutableListOf<File>()
        arrData.addAll(fileManagerAdapter.list)
        bindingRight.rcvFolder.layoutManager = layoutManager

        fileManagerAdapter = FileManagerAdapter(context, arrData)
        fileManagerAdapter.isHorizoltal = viewModel.folderIsGridView.value != true
        viewModel.lstFolderSelected.value?.let { data ->
            fileManagerAdapter.stackItemSelected = data
        }
        bindingRight.rcvFolder.adapter = fileManagerAdapter
        initListenerAdapter()
    }

    private fun updateAdapter(path: String) {
        bindingRight.scvFolder.visibility = View.INVISIBLE
        bindingRight.scvThisPc.visibility = View.GONE
        bindingRight.lnLocalDiskC.visibility = View.GONE
        bindingRight.lnRightFtp.visibility = View.GONE
        bindingRight.lnRightLan.visibility = View.GONE

        when (path) {
            Config.FileManager.thisQuickAccessPath -> {
                bindingRight.scvFolder.visibility = View.VISIBLE
                viewModel.totalFileInFolder.postValue(viewModel.lstQuickAccess)
                fileManagerAdapter.apply {
                    clear()
                    addAll(viewModel.lstQuickAccess)
                }.notifyDataSetChanged()
                val state: Parcelable? =
                    listPosition.getParcelable(viewModel.getKeyBundleStageRcv())
                bindingRight.rcvFolder.layoutManager?.onRestoreInstanceState(state)
            }
            Config.FileManager.thisUserPath -> {
                bindingRight.scvThisPc.visibility = View.VISIBLE
            }
            Config.FileManager.thisPCPath -> {
                bindingRight.scvThisPc.visibility = View.VISIBLE
                bindingRight.lnLocalDiskC.visibility = View.VISIBLE
            }
            Config.FileManager.thisFtpPath -> {
                bindingRight.lnRightFtp.visibility = View.VISIBLE
            }
            Config.FileManager.thisLanPath -> {
                bindingRight.lnRightLan.visibility = View.VISIBLE
            }
            else -> {
                bindingRight.scvFolder.visibility = View.VISIBLE
                fileManagerAdapter.clear()
                File(path).listFiles()?.filter { item ->
                    !item.isHidden
                }?.let { lstFolder ->
                    fileManagerAdapter.addAll(lstFolder.toMutableList())
                }
                viewModel.totalFileInFolder.postValue(fileManagerAdapter.list)
                fileManagerAdapter.notifyDataSetChanged()
                val state: Parcelable? =
                    listPosition.getParcelable(viewModel.getKeyBundleStageRcv())
                bindingRight.rcvFolder.layoutManager?.onRestoreInstanceState(state)
            }
        }
    }

    private fun initListenerAdapter() {
        fileManagerAdapter.itemClickListener = {
            when {
                stageSelected -> {
                    viewModel.pushFolderSelected(it)
                }
                it.isDirectory -> {

                    val currentPosition: Parcelable? =
                        bindingRight.rcvFolder.layoutManager?.onSaveInstanceState()
                    listPosition.putParcelable(viewModel.getKeyBundleStageRcv(), currentPosition)
                    viewModel.pushDataUndo(it.path)

                    EzAdControl.getInstance(context as AppCompatActivity).showAds()
                }
                else -> {
                    try {
                        it.openFile(context)
                    } catch (ex: Exception) {
                        toast(context.getString(R.string.cant_open_file))
                    }
                }
            }
        }
        fileManagerAdapter.itemLongClickListener = {
            if (!stageSelected) {
                stageSelected = true
                viewModel.pushFolderSelected(it)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewModel.stackFolderUndo.removeObserver { }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initControl() {
        bindingRight.lnLocalDiskC.setOnClickListener(this)
        bindingRight.fvRightDocument.setOnClickListener(this)
        bindingRight.fvRightDowloads.setOnClickListener(this)
        bindingRight.fvRightVideo.setOnClickListener(this)
        bindingRight.fvRightPicture.setOnClickListener(this)
        bindingRight.btnStatusFtp.setOnClickListener(this)
        bindingRight.scvFolder.setOnTouchListener { _, _ -> true }
//        bindingRight.scvThisPc.setOnTouchListener { _, _ -> true }
        initListenerAdapter()

        try {
            context.unregisterReceiver(broadCastListener)
        } catch (e: java.lang.Exception) {
        }
        broadCastListener.disconnectListener = {
            setStageFtp(false)
        }
        val intentFilterControl = IntentFilter()
        intentFilterControl.addAction(Config.Notification.ActionClickNotification)
        context.registerReceiver(broadCastListener, intentFilterControl)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            /*Center right*/
            R.id.ln_local_disk_c -> {

                viewModel.pushDataUndo(Config.FileManager.externalStoragePath)
            }
            R.id.fv_right_document -> {

                openFolderByPath(Config.FileManager.documentPath)
            }
            R.id.fv_right_dowloads -> {

                openFolderByPath(Config.FileManager.downloadPath)
            }
            R.id.fv_right_video -> {

                openFolderByPath(Config.FileManager.videoPath)
            }
            R.id.fv_right_picture -> {

                openFolderByPath(Config.FileManager.picturePath)
            }
            R.id.btn_status_ftp -> {
                FTPServerService.isRunning().apply {
                    Intent(context, FTPServerService::class.java).let {
                        if (this) {
                            context.stopService(it)
                            setStageFtp(false)
                        } else {
                            if (Utils.isNetworkAvailable(context)) {
                                context.startService(it)
                                setStageFtp(true)
                            } else {
                                toast(context.getString(R.string.network_available))
                            }
                        }
                    }
                }
            }
        }
        EzAdControl.getInstance(context as AppCompatActivity).showAds()
    }

    private fun setStageFtp(isRunning: Boolean) {
        bindingRight.btnStatusFtp.text =
            if (isRunning) context.getString(R.string.stop)
            else context.getString(R.string.start)
        bindingRight.tvStatusFtp.text =
            if (isRunning) Html.fromHtml(ftpInformation)
            else Html.fromHtml(context.getString(R.string.start_server_ftp))
    }

    private val ftpInformation: String
        get() {
            val stringBuilder = StringBuilder()
            stringBuilder.append(context.getString(R.string.ftp_detail))
            stringBuilder.append("<br/><br/>")
            stringBuilder.append(
                context.getString(
                    R.string.wifi_url,
                    "ftp://" + InternetHelper.getIPAddress() + ":2121"
                )
            )
            stringBuilder.append("<br/><br/>")
            stringBuilder.append(context.getString(R.string.user_name, "admin"))
            stringBuilder.append("<br/><br/>")
            stringBuilder.append(context.getString(R.string.password, "123456"))
            return stringBuilder.toString()
        }
}