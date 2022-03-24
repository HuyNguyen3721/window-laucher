package com.ezteam.windowslauncher

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS
import android.view.LayoutInflater
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.windowslauncher.databinding.ActivityBackGroundBinding
import com.ezteam.windowslauncher.screen.MainActivity

class BackGroundActivity : BaseActivity<ActivityBackGroundBinding>() {
    private val REQUEST_CODE_WRITE_SETTINGS = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackGroundBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        val intent = intent
        when {
            intent.getStringExtra("PERMISSION") == "WRITE_SETTING" -> {
                requestPermissionWriteSetting()
            }
        }
    }

    private fun requestPermissionWriteSetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + this.packageName)
            startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            finish()
        }
    }

    override fun initView() {
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun viewBinding(): ActivityBackGroundBinding {
        return ActivityBackGroundBinding.inflate(LayoutInflater.from(this))
    }


}