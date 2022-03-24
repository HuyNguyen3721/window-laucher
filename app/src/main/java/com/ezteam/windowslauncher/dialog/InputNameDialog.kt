package com.ezteam.windowslauncher.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.ezteam.baseproject.dialog.BaseDialog
import com.ezteam.baseproject.dialog.BuilderDialog
import com.ezteam.windowslauncher.databinding.DialogInputNameBinding

class InputNameDialog(
    extendBuilder: ExtendBuilder,
     context : Context
) : BaseDialog<DialogInputNameBinding, InputNameDialog.ExtendBuilder>(extendBuilder,context) {
    companion object {
        const val INPUT_NAME = "input_name"
    }

    class ExtendBuilder(context: Context) : BuilderDialog(context) {
        internal var fileName: String = ""

        override fun build(): BaseDialog<*, *> {
            return InputNameDialog(this,context)
        }

        fun setFileName(fileName: String): ExtendBuilder {
            this.fileName = fileName
            return this
        }
    }

    override fun initView() {
        super.initView()
        binding.edtInputName.setText(builder.fileName)
        binding.edtInputName.selectAll()
    }

    override fun initListener() {

    }

    override fun handleClickPositiveButton(data: HashMap<String?, Any?>) {
        val inputName = binding.edtInputName.text.toString()
        if (inputName.isNotEmpty()) {
            data[INPUT_NAME] = inputName
            super.handleClickPositiveButton(data)
        }
        dismiss()
    }

    override fun handleClickNegativeButton(view: View) {
        super.handleClickNegativeButton(view)
        dismiss()
    }

    override val viewBinding: DialogInputNameBinding
        get() = DialogInputNameBinding.inflate(LayoutInflater.from(context))

    override val title: TextView
        get() = binding.tvTitle

    override val positiveButton: TextView
        get() = binding.tvPositive

    override val negativeButton: TextView
        get() = binding.tvNegative
}