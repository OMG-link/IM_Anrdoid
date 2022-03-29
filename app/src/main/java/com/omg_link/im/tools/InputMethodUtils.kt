package com.omg_link.im.tools

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


object InputMethodUtils {

    private fun getInputMethodManager(context: Context):InputMethodManager?{
        return context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    }

    fun isInputMethodActive(context: Context):Boolean{
        val imm = getInputMethodManager(context)
        return imm?.isActive ?: false
    }

    fun hideInputMethod(activity: Activity,view: View){
        val imm = getInputMethodManager(activity)
            ?: return
        if(isInputMethodActive(activity)){
            imm.hideSoftInputFromWindow(view.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

}