package com.personal.chatroommobile.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.personal.chatroommobile.R
import com.personal.chatroommobile.data.CacheData

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val toolbar: Toolbar = findViewById<View>(R.id.chat_toolbar) as Toolbar
        val toolbarImage: ShapeableImageView = findViewById(R.id.toolbar_image)
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        Glide.with(this)
            .load(CacheData.contact.image)
            .into(toolbarImage)

        toolbarTitle.text = CacheData.contact.name
        toolbar.setNavigationIcon(R.drawable.ic_back)

        toolbar.setNavigationOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                MessageFragment()
            ).commit();
        }
    }
}