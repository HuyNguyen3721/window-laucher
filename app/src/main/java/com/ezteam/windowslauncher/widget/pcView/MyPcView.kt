package com.ezteam.windowslauncher.widget.pcView

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.ezteam.baseproject.extensions.loadAnimation
import com.ezteam.baseproject.extensions.resizeViewSmooth
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutBottomSelectFolderBinding
import com.ezteam.windowslauncher.databinding.LayoutMyComputerBinding
import com.ezteam.windowslauncher.utils.Config
import kotlin.math.abs

class MyPcView(context: Context) : BaseFileManagerView(context), View.OnClickListener {

    private var binding = LayoutMyComputerBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_my_computer, this)
    )
    private val bindingBottomSelect =
        LayoutBottomSelectFolderBinding.bind(binding.layoutSelect.root)

    var closeListener: ((Unit) -> Unit)? = null
    private var isExpand = true
    private var minW = 300
    private var minH = 500

    private var firstW = 0.0f
    private var firstH = 0.0f

    private var lastX = 0
    private var lastY = 0
    private var lastWidth = 0.0f
    private var lastHeight = 0.0f
    private var xDelta = 0
    private var yDelta = 0
    private var lastTimeClick = 0L

    init {
        initView()
        initControl()
        resetAllView()
    }

    private fun initView() {
        post {
            if (height / 2 > 500)
                minH = height / 2

            if (2 * width / 3 > 300)
                minW = 2 * width / 3

            firstW = width.toFloat()
            firstH = height.toFloat()
            lastWidth = firstW
            lastHeight = firstH
            resizeViewSmooth(
                firstW,
                firstH,
                0.0f,
                0.0f, 0
            )
        }
    }

    fun resetAllView() {
        viewModel.resetViewModel()
        bindingBottomSelect.root.visibility = View.GONE
    }

    fun openUserFolder() {
        resetAllView()
        viewModel.pushDataUndo(Config.FileManager.thisUserPath)
    }

    fun openPcFolder() {
        resetAllView()
        viewModel.pushDataUndo(Config.FileManager.thisPCPath)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initControl() {
        binding.pcViewTop.closeListener = {
            loadAnimation(R.anim.close_view, onEnd = {
                closeListener?.invoke(Unit)
                this.requestLayout()
            })
        }
        binding.pcViewTop.collapseListener = {
            setStageView()
        }
        binding.pcViewTop.hideListener = {
            showHideView(false, firstW / 2, firstH)
        }
        binding.frmBottom.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                (layoutParams.height + event.y.toInt()).apply {
                    if (this >= minH) {
                        layoutParams.height = this
                        lastHeight = layoutParams.height.toFloat()
                        requestLayout()
                        binding.bannerAds.isVisible = lastWidth >= firstW
                    }
                }
            }
            true
        }

        binding.frmRight.setOnTouchListener { _, event ->
            (layoutParams.width + event.x.toInt()).apply {
                if (this >= minW) {
                    layoutParams.width = this
                    lastWidth = layoutParams.width.toFloat()
                    requestLayout()

                    binding.bannerAds.isVisible = lastWidth >= firstW
                }
            }
            true
        }

        bindingBottomSelect.ivCloseSelect.setOnClickListener(this)
        bindingBottomSelect.rdSelectAll.setOnClickListener(this)
    }

    private fun setStageView() {
        if (isExpand) {
            if (abs(lastWidth - firstW) < 50 && abs(lastHeight - firstH) < 50) {
                resizeViewSmooth(
                    firstW - 100.0f,
                    firstH / 2,
                    50.0f,
                    firstH / 2 - 100.0f
                )
            } else {
                resizeViewSmooth(
                    lastWidth,
                    lastHeight,
                    lastX.toFloat(),
                    lastY.toFloat()
                )
            }
            binding.bannerAds.isVisible = false
        } else {
            resizeViewSmooth(
                firstW,
                firstH,
                0.0f,
                0.0f
            )
            binding.bannerAds.isVisible = true
        }
        isExpand = !isExpand
    }

    fun showHideView(isShow: Boolean, x: Float = firstW, y: Float = firstH) {
        if (isShow) {
            resizeViewSmooth(
                x,
                y,
                0.0f,
                0.0f
            )
            isExpand = true
        } else {
            resizeViewSmooth(
                0.0f,
                0.0f,
                x,
                y
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel.lstFolderSelected.observeForever {
            if (it.isNotEmpty()) {
                bindingBottomSelect.tvCountSelect.text =
                    context.getString(R.string.item_selected, it.size)
                ViewUtils.showView(false, bindingBottomSelect.root, 300)
                bindingBottomSelect.rdSelectAll.isChecked =
                    it.size == viewModel.totalFileInFolder.value?.size
            } else {
                bindingBottomSelect.rdSelectAll.isChecked = false
                ViewUtils.hideView(false, bindingBottomSelect.root, 300)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (System.currentTimeMillis() - lastTimeClick < 200) {
                    setStageView()
                }
                lastTimeClick = System.currentTimeMillis()
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                val lParams: FrameLayout.LayoutParams =
                    layoutParams as FrameLayout.LayoutParams
                xDelta = lastX - lParams.leftMargin
                yDelta = lastY - lParams.topMargin
            }
            MotionEvent.ACTION_MOVE -> {
                val dx: Int = event.rawX.toInt() - lastX
                val dy: Int = event.rawY.toInt() - lastY
                val left: Int = left + dx
                val top: Int = top + dy
                val right: Int = right + dx
                val bottom: Int = bottom + dy
                layout(left, top, right, bottom)
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
            }
            MotionEvent.ACTION_UP -> {
                val params: FrameLayout.LayoutParams =
                    layoutParams as FrameLayout.LayoutParams
                params.leftMargin = lastX - xDelta
                params.topMargin = lastY - yDelta
                params.rightMargin = 0
                params.bottomMargin = 0
                layoutParams = params
                lastWidth = layoutParams.width.toFloat()
                lastHeight = layoutParams.height.toFloat()
                lastX = x.toInt()
                lastY = y.toInt()
                requestLayout()
                if (System.currentTimeMillis() - lastTimeClick > 300) {
                    isExpand = false
                }
            }
        }
        return true
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.iv_close_select -> {
                viewModel.cleanStackSelected()
            }
            R.id.rd_select_all -> {
                val isSelectAll =
                    viewModel.totalFileInFolder.value?.size == viewModel.lstFolderSelected.value?.size
                bindingBottomSelect.rdSelectAll.isChecked = !isSelectAll
                viewModel.lstFolderSelected.postValue(
                    if (!isSelectAll) {
                        viewModel.totalFileInFolder.value
                    } else {
                        mutableListOf()
                    }
                )
            }
        }
    }

}