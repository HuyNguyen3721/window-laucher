package com.ezteam.windowslauncher.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.baseproject.extensions.getDisplayMetrics
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.ItemLauncherHorizontalBinding
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.utils.launcher.LauncherAppUtils
import com.ezteam.windowslauncher.utils.launcher.LauncherType


class ItemLauncherDragAdapter(
    context: Context,
    items: MutableList<LauncherModel>,
    var itemClickListener: (LauncherModel) -> Unit,
    var itemLongClickListener: (View, LauncherModel) -> Unit,
    var itemDragAndDropListener: (MutableList<LauncherModel>) -> Unit,
    var itemOnMove: (() -> Unit)? = null
) : BaseRecyclerAdapter<LauncherModel, ItemLauncherDragAdapter.ViewHolder>(context, items) {
    companion object {
        const val TAG = "ItemDrag"
    }

    inner class ViewHolder(
        var binding: ItemLauncherHorizontalBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private var eventX = -1f
        private var eventY = -1f

        @SuppressLint("ClickableViewAccessibility")
        fun bindData(launcher: LauncherModel) {
            val drawable: Drawable? = mContext.packageManager.getDrawable(
                launcher.packageName,
                launcher.logoResId, null
            )

            val displayWidth = mContext.getDisplayMetrics().widthPixels
            binding.root.apply {
                layoutParams.width = displayWidth / LauncherAppUtils.getSizeGrid(context).columns
                Log.e("Columns", "${LauncherAppUtils.getSizeGrid(mContext).columns}")
            }

            val paddingLarge = mContext.resources.getDimensionPixelOffset(R.dimen._7sdp)
            val paddingNormal = mContext.resources.getDimensionPixelOffset(R.dimen._5sdp)

            if (launcher.launcherType == LauncherType.APP_SYSTEM.value) {
                drawable?.let {
                    binding.ivLauncher.setImageDrawable(it)
                    binding.ivLauncher.setPadding(
                        paddingLarge,
                        paddingLarge,
                        paddingLarge,
                        paddingNormal
                    )
                }
            } else {
                binding.ivLauncher.setImageResource(launcher.logoResId)
                binding.ivLauncher.setPadding(paddingNormal)
            }

            binding.tvLauncher.text = launcher.appName

            binding.root.setOnClickListener {
                itemClickListener(launcher)
            }
        }
    }

    val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0
    ) {
        var oldPosition = -1
        var newPosition = -1

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            newPosition = target.adapterPosition
            itemOnMove?.invoke()
            return false
        }

        override fun onMoved(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            fromPos: Int,
            target: RecyclerView.ViewHolder,
            toPos: Int,
            x: Int,
            y: Int
        ) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
            Log.d(TAG, "Movexxx")
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            Log.d(TAG, "Swiped")
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            try {
                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_DRAG -> {
                        viewHolder?.adapterPosition?.let {
                            oldPosition = it
                            if (viewHolder is ViewHolder) {
                                itemLongClickListener(viewHolder.binding.ivLauncher, list[it])
                            }
                        }
                        Log.d(TAG, "Drag")
                    }
                    ItemTouchHelper.ACTION_STATE_IDLE -> {
                        if (oldPosition != -1 && newPosition != -1 && oldPosition != newPosition) {
                            val old = list[oldPosition]
                            val new = list[newPosition]

                            if (old.launcherType == LauncherType.APP_IDLE.value) {
                                return
                            }

                            old.position = newPosition
                            new.position = oldPosition
                            list[oldPosition] = new
                            list[newPosition] = old

                            notifyDataSetChanged()
                            itemDragAndDropListener(
                                mutableListOf(
                                    new, old
                                )
                            )
                            oldPosition = -1
                            newPosition = -1
                        }
                    }
                }
            } catch (e: ArrayIndexOutOfBoundsException) {
            }
        }

        override fun isLongPressDragEnabled(): Boolean {
            Log.d(TAG, "LongPress")
            return super.isLongPressDragEnabled()
        }
    })

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLauncherHorizontalBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
}