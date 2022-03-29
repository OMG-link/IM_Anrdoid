package com.omg_link.im.tools

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.omg_link.im.R
import java.io.File


object AndroidUtils {
    fun hasPermission(appCompatActivity: AppCompatActivity, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            appCompatActivity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * @author https://blog.csdn.net/DucklikeJAVA/article/details/48548109
     */
    fun openFile(file: File, context: Context, mimeType: String?): Boolean {
        try {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // 设置intent的Action属性
            intent.action = Intent.ACTION_VIEW
            // 设置intent的data和Type属性。
            intent.setDataAndType(
                UriUtils.getUriFromFile(file,context),
                mimeType
            )
            // 跳转
            context.startActivity(intent) // 这里最好try一下，有可能会报错。
            // //比如说你的MIME类型是打开邮箱，但是你手机里面没装邮箱客户端，就会报错。
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.activity_room_cannot_open_file, Toast.LENGTH_SHORT).show()
            return false;
        }
        return true;
    }

    fun openFile(file: File, context: Context): Boolean{
        // 获取文件file的MIME类型
        val fileExtensionFromUrl = MimeTypeMap
            .getFileExtensionFromUrl(Uri.fromFile(file).toString())
        val mimeTypeFromExtension = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(fileExtensionFromUrl)
        return openFile(file,context,mimeTypeFromExtension)
    }

    fun getApkVersion(context: Context):String {
        return context.packageManager.getPackageInfo(context.packageName,0).versionName
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun dip2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    fun px2sp(context: Context, pxValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

}