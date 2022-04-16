package com.omg_link.im.android.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory

object BitmapUtils {
    private val map:MutableMap<String,Bitmap> = HashMap()

    fun getBitmap(path:String, forceRefresh: Boolean=false):Bitmap?{
        return if(!forceRefresh&&map.containsKey(path)){
            map[path]
        }else{
            val bitmap:Bitmap? = BitmapFactory.decodeFile(path)
            if(bitmap!=null){
                map[path] = bitmap
            }
            bitmap
        }
    }

}