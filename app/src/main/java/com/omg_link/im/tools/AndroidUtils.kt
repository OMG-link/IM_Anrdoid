package com.omg_link.im.tools

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File


object AndroidUtils {
    fun hasPermission(appCompatActivity: AppCompatActivity, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            appCompatActivity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request a permission.
     * @return true if the request has been send
     */
    fun requestPermission(appCompatActivity: AppCompatActivity, permission: String): Boolean {
        if (hasPermission(appCompatActivity, permission)) return true
        ActivityCompat.requestPermissions(appCompatActivity, arrayOf(permission), 1)
        return false
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

}