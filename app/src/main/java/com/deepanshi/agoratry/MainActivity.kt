package com.deepanshi.agoratry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {

    lateinit var channel:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        channel= intent.getStringExtra("CHANNEL_NAME_SWIPED").toString()
        if(channel!="null"){
            et_channel.setText(channel)
        }
        else{
            et_channel.text?.clear()
        }


        btn_create_room.setOnClickListener {
            if(et_channel.text.toString().length>=5) {
                val i = Intent(this, VidCallActivity::class.java)
                i.putExtra("CHANNEL_NAME", et_channel.text.toString())
                startActivity(i)
            }
            else{
                showErrorSnackBar("Channel name must have at least 5 characters")
            }
        }

        setupActionBar()
    }


    private fun setupActionBar(){
        setSupportActionBar(toolbar_main_activity)

        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
        }

        toolbar_main_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}