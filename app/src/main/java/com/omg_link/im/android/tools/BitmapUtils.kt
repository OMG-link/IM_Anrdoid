package com.omg_link.im.android.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory

object BitmapUtils {
    private val map:MutableMap<String,Bitmap> = HashMap()

    fun getBitmap(path:String):Bitmap?{
        return if(map.containsKey(path)){
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