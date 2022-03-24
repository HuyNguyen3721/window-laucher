package com.ezteam.windowslauncher.widget.pcView.center

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutPcCenterLeftBinding
import com.ezteam.windowslauncher.utils.Config
import com.ezteam.windowslauncher.widget.pcView.BaseFileManagerView
import com.google.android.gms.ads.ez.EzAdControl
import java.io.File


class PcViewCenterLeft(context: Context, attrs: AttributeSet?) :
    BaseFileManagerView(context, attrs),
    View.OnClickListener {

    private var bindingLeft = LayoutPcCenterLeftBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_pc_center_left, this)
    )

    init {
        initView()
        initControl()
    }

    private fun initView() {

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel.expendLeftLayout.observeForever {
            if (it) {
                bindingLeft.ivExpend.setImageResource(R.drawable.ic_left_arrow)
            } else {
                bindingLeft.ivExpend.setImageResource(R.drawable.ic_right_arrow)
                ViewUtils.collapse(bindingLeft.lnExpendQuickAccess)
                ViewUtils.rotate(bindingLeft.ivStatusQuickAccess, 90.0f, 0.0f)
                ViewUtils.rotate(bindingLeft.ivStatusNetwork, 90.0f, 0.0f)
                ViewUtils.collapse(bindingLeft.lnExpendNetwork)
            }
        }
    }

    private fun initControl() {
        bindingLeft.lnQuickAction.setOnClickListener(this)
        bindingLeft.lnNetwork.setOnClickListener(this)
        bindingLeft.fvLocalDiskC.setOnClickListener(this)
        bindingLeft.fvThisPc.setOnClickListener(this)
        bindingLeft.fvLeftDestop.setOnClickListener(this)
        bindingLeft.fvLeftDocument.setOnClickListener(this)
        bindingLeft.fvLeftDownload.setOnClickListener(this)
        bindingLeft.fvLeftPicture.setOnClickListener(this)
        bindingLeft.fvLeftFtp.setOnClickListener(this)
        bindingLeft.fvLeftLan.setOnClickListener(this)
        bindingLeft.ivExpend.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            /*Center left*/
            R.id.ln_quick_action -> {
                viewModel.pushDataUndo(Config.FileManager.thisQuickAccessPath)
                viewModel.cleanStackSelected()
                bindingLeft.lnExpendQuickAccess.apply {
                    if (isShown) {
                        ViewUtils.collapse(this)
                        ViewUtils.rotate(bindingLeft.ivStatusQuickAccess, 90.0f, 0.0f)
                    } else {
                        viewModel.expendLeftLayout.value = true
                        ViewUtils.rotate(bindingLeft.ivStatusQuickAccess, 0.0f, 90.0f)
                        ViewUtils.expand(this)
                    }
                }
            }
            R.id.ln_network -> {
                bindingLeft.lnExpendNetwork.apply {
                    if (isShown) {
                        ViewUtils.rotate(bindingLeft.ivStatusNetwork, 90.0f, 0.0f)
                        ViewUtils.collapse(this)
                    } else {
                        viewModel.expendLeftLayout.value = true
                        ViewUtils.rotate(bindingLeft.ivStatusNetwork, 0.0f, 90.0f)
                        ViewUtils.expand(this)
                    }
                }
            }
            R.id.fv_local_disk_c -> {
                viewModel.pushDataUndo(Config.FileManager.externalStoragePath)
                viewModel.cleanStackSelected()
            }
            R.id.fv_this_pc -> {
                viewModel.pushDataUndo(Config.FileManager.thisPCPath)
                viewModel.cleanStackSelected()
            }
            R.id.fv_left_destop -> {
                openFolderByPath(Config.FileManager.desktopPath(context))
                viewModel.cleanStackSelected()
            }

            R.id.fv_left_document -> {
                openFolderByPath(Config.FileManager.documentPath)
                viewModel.cleanStackSelected()
            }

            R.id.fv_left_download -> {
                openFolderByPath(Config.FileManager.downloadPath)
                viewModel.cleanStackSelected()
            }

            R.id.fv_left_picture -> {
                openFolderByPath(Config.FileManager.picturePath)
                viewModel.cleanStackSelected()
            }

            R.id.fv_left_ftp -> {
                viewModel.pushDataUndo(Config.FileManager.thisFtpPath)
                viewModel.cleanStackSelected()
            }

            R.id.fv_left_lan -> {
                viewModel.pushDataUndo(Config.FileManager.thisLanPath)
                viewModel.cleanStackSelected()
            }

            R.id.iv_expend -> {
                viewModel.expendLeftLayout.value = !viewModel.expendLeftLayout.value!!
            }
        }
        EzAdControl.getInstance(context as AppCompatActivity).showAds()
    }

}