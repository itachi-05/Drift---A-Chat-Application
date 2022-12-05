package com.example.driftdb

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.driftdb.databinding.ActivityImageZoomBinding


class UserImageZoom: AppCompatActivity() {
    private lateinit var binding: ActivityImageZoomBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageZoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


//        val byteArray = intent.getByteArrayExtra("image")
//        val bmp = BitmapFactory.decodeByteArray(byteArray , 0 , byteArray!!.size)
//        binding.zoomedImage.setImageBitmap(bmp)
    }
}