package com.omg_link.im.android

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import com.github.chrisbanes.photoview.PhotoView
import com.omg_link.im.R
import com.omg_link.im.android.tools.BitmapUtils

class ImageViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        val imagePath = intent.getStringExtra("ImagePath")!!
        val photoView = findViewById<PhotoView>(R.id.fullScreenImageView)
        photoView.setImageBitmap(BitmapUtils.getBitmap(imagePath))
        photoView.setZoomable(true)
        photoView.scaleType = ImageView.ScaleType.CENTER
        photoView.setOnClickListener {
            finish()
        }

    }
}