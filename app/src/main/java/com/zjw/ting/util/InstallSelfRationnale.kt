package com.zjw.ting.util

import android.app.AlertDialog
import android.content.Context
import android.text.TextUtils
import com.eye.cool.permission.Permission
import com.eye.cool.permission.PermissionSetting
import com.eye.cool.permission.Rationale
import com.zjw.ting.R

class InstallSelfRationnale : Rationale {
    override fun showRationale(context: Context, permissions: Array<String>, callback: ((result: Boolean) -> Unit)?) {

        val permissionNames = Permission.transformText(context, permissions)
        val message = context.getString(
            R.string.permission_setting_rationale,
            getAppName(context), TextUtils.join("\n", permissionNames)
        )

        val settingService = PermissionSetting(context)

        AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle(R.string.permission_title_rationale)
            .setMessage(message)
            .setPositiveButton(R.string.permission_setting) { _, _ -> settingService.start() }
            .setNegativeButton(R.string.permission_no) { _, _ -> settingService.cancel() }
            .show()
    }

    fun getAppName(context: Context): String {
        val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, 0) ?: return ""
        return context.packageManager.getApplicationLabel(applicationInfo) as? String ?: ""
    }
}