package com.ezteam.windowslauncher.dialog

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.ezteam.baseproject.dialog.BaseDialog
import com.ezteam.baseproject.dialog.BuilderDialog
import com.ezteam.windowslauncher.databinding.DialogInputPasswordBinding

class InputPasswordDialog(
    extendBuilder: ExtendBuilder
) : BaseDialog<DialogInputPasswordBinding, InputPasswordDialog.ExtendBuilder>(extendBuilder) {
    companion object {
        const val INPUT_PASSWORD = "input_password"
    }

    class ExtendBuilder : BuilderDialog() {
        internal var fileName: String = ""

        override fun build(): BaseDialog<*, *> {
            return InputPasswordDialog(this)
        }

        fun setFileName(fileName: String): ExtendBuilder {
            this.fileName = fileName
            return this
        }
    }

    override fun initView() {
        super.initView()
        binding.edtInputPassword.setText(builder.fileName)
        binding.edtInputPassword.selectAll()
    }

    override fun initListener() {

    }

    override fun handleClickPositiveButton(data: HashMap<String?, Any?>) {
        val inputName = binding.edtInputPassword.text.toString()
        if (inputName.isNotEmpty()) {
            data[INPUT_PASSWORD] = inputName
            super.handleClickPositiveButton(data)
        }
        dismiss()
    }

    override fun handleClickNegativeButton(view: View) {
        super.handleClickNegativeButton(view)
        dismiss()
    }

    override val viewBinding: DialogInputPasswordBinding
        get() = DialogInputPasswordBinding.inflate(LayoutInflater.from(context))

    override val title: TextView
        get() = binding.tvTitle

    override val positiveButton: TextView
        get() = binding.tvPositive

    override val negativeButton: TextView
        get() = binding.tvNegative
}