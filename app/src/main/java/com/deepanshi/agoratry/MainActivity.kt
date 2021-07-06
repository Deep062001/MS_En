package com.deepanshi.agoratry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        et_channel.addTextChangedListener{
           // var uid=et_uid.text.toString()
            btn_create_room.isEnabled=!(et_channel.length()<5)
        }

        btn_create_room.setOnClickListener {
            val i= Intent(this,MainActivity2::class.java)
            i.putExtra("CHANNEL_NAME",et_channel.text.toString())
            i.putExtra("UID",et_uid.text)
            startActivity(i)
        }
    }
}