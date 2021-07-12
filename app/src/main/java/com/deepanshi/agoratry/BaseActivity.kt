package com.deepanshi.agoratry

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
//import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.dialog_progress.*

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce=false

    private lateinit var mProgressDialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }


    fun showErrorSnackBar(message: String){
        val snackBar=Snackbar.make(findViewById(android.R.id.content),
        message,
        Snackbar.LENGTH_LONG)

        val snackbarView=snackBar.view
        snackbarView.setBackgroundColor(ContextCompat.getColor(this,R.color.snackbar_color))

        snackBar.show()

    }






}