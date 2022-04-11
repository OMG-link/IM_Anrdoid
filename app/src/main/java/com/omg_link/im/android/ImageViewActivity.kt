package com.omg_link.im.android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.github.chrisbanes.photoview.PhotoView
import com.omg_link.im.R
import com.omg_link.im.android.tools.BitmapUtils
import com.omg_link.im.core.Client
import com.omg_link.im.core.ClientRoom
import java.util.*
import java.util.logging.Level
import kotlin.math.max
import kotlin.math.min

class ImageViewActivity : AppCompatActivity() {

    private val client: Client

    private lateinit var layout: FrameLayout
    private lateinit var photoView: PhotoView
    private lateinit var bitmap: Bitmap

    init {
        val activeClient = MainActivity.getActiveClient()
        if (activeClient != null) {
            client = activeClient
        } else {
            throw RuntimeException("Failed to initialize ImageViewActivity: Main client not found!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        layout = findViewById(R.id.layoutImageViewBackground)
        photoView = findViewById(R.id.fullScreenImageView)

        val imagePath = intent.getStringExtra("ImagePath")!!
        bitmap = BitmapUtils.getBitmap(imagePath)!!

        photoView.setImageBitmap(bitmap)
        photoView.setZoomable(true)
        photoView.scaleType = ImageView.ScaleType.CENTER
        photoView.setOnClickListener {
            finish()
        }

    }

    override fun onResume() {
        super.onResume()

        // Need a delay, don't know why.
        Timer().schedule(object : TimerTask(){
            override fun run() {
                runOnUiThread {
                    val screenWidth:Float = resources.displayMetrics.widthPixels.toFloat()
                    val screenHeight:Float = resources.displayMetrics.heightPixels.toFloat()
                    val imageWidth:Float = bitmap.width.toFloat()
                    val imageHeight:Float = bitmap.height.toFloat()

                    var scale = min(screenWidth/imageWidth,screenHeight/imageHeight)
                    scale = min(scale,4.0f)
                    photoView.maximumScale = max(scale,4.0f)
                    photoView.minimumScale = min(scale,1.0f)
                    photoView.setScale(scale,false)
                }
            }
        },100)

    }

}