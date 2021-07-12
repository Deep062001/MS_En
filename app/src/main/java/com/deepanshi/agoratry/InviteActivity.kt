package com.deepanshi.agoratry

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_invite.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

const val DB_NAME="meeting.db"
class InviteActivity : BaseActivity(),View.OnClickListener {



    lateinit var myCalendar: Calendar
    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener
    var finalDate = 0L
    var finalTimeFrom = 0L
    var finalTimeTill = 0L

    val db by lazy{
        AppDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite)


        et_date.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        et_time_from.setOnClickListener(this)
        et_time_till.setOnClickListener(this)
        btn_invite.setOnClickListener(this)

        setupActionBar()

    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_invite_activity)

        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
        }

        toolbar_invite_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    private fun shareText(invitation: String) {
        val intent = Intent(Intent.ACTION_SEND)

        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        intent.putExtra(Intent.EXTRA_TEXT, invitation)
        startActivity(Intent.createChooser(intent, "Share Via"))

    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.btn_invite->{

                val mName=et_Name.text.toString()
                val mDate=et_date.text.toString()
                val mFrom=et_time_from.text.toString()
                val mTill=et_time_till.text.toString()
                val mChannel=et_Channel_name.text.toString()


                if(validateForm(mName,mChannel,mDate,mFrom,mTill)){
                    val s=getString(R.string.invite,mName,mDate,mFrom,mTill,mChannel)
                    shareText(s)
                }

            }

            R.id.et_date->{
                setDateListener()
            }

            R.id.et_time_from->{
                setTimeListener(R.id.et_time_from)
            }

            R.id.et_time_till->{
                setTimeListener(R.id.et_time_till)
            }

            R.id.btn_save->{
                val mName=et_Name.text.toString()
                val mDate=et_date.text.toString()
                val mFrom=et_time_from.text.toString()
                val mTill=et_time_till.text.toString()
                val mChannel=et_Channel_name.text.toString()


                if(validateForm(mName,mChannel,mDate,mFrom,mTill)){
                    val s=getString(R.string.invite,mName,mDate,mFrom,mTill,mChannel)
                    saveMeet()
                }

            }
        }
    }

    private fun saveMeet() {
        val name=et_Name.text.toString()
        val channel_name=et_Channel_name.text.toString()

        GlobalScope.launch(Dispatchers.Main) {
            val id = withContext(Dispatchers.IO) {
                return@withContext db.meetingDao().insertMeet(
                    MeetingModel(
                        name,
                        channel_name,
                        finalDate,
                        finalTimeFrom,
                        finalTimeTill
                    )
                )
            }
            finish()
        }

    }


    private fun setDateListener() {
        myCalendar = Calendar.getInstance()

        dateSetListener =
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDate()

            }

        val datePickerDialog = DatePickerDialog(
            this, dateSetListener, myCalendar.get(Calendar.YEAR),
            myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDate() {
        val myformat = "EEE, d MMM yyyy"
        val sdf = SimpleDateFormat(myformat)
        finalDate=myCalendar.time.time

        et_date.setText(sdf.format(myCalendar.time))

        ll_times.visibility=View.VISIBLE
    }

    private fun setTimeListener(Id: Int) {
        myCalendar = Calendar.getInstance()

        timeSetListener =
            TimePickerDialog.OnTimeSetListener() { _: TimePicker, hourOfDay: Int, min: Int ->
                myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                myCalendar.set(Calendar.MINUTE, min)
                updateTime(Id)
            }

        val timePickerDialog = TimePickerDialog(
            this, timeSetListener, myCalendar.get(Calendar.HOUR_OF_DAY),
            myCalendar.get(Calendar.MINUTE), false
        )

        timePickerDialog.show()
    }

    private fun updateTime(Id: Int) {
        var et=findViewById<EditText>(Id)
        val myformat = "hh:mm a"
        val sdf = SimpleDateFormat(myformat)
        if(Id==R.id.et_time_from){
            finalTimeFrom=myCalendar.time.time
        }
        else{
            finalTimeTill=myCalendar.time.time
        }
        et.setText(sdf.format(myCalendar.time))
    }

    private fun validateForm(name:String, channel: String, date:String,time1: String,time2: String):Boolean{
        return when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(channel) || channel.length<5->{
                showErrorSnackBar("Please enter Channel Name with at least 5 characters")
                false
            }
            TextUtils.isEmpty(date)->{
                showErrorSnackBar("Please enter Date")
                false
            }
            TextUtils.isEmpty(time1)->{
                showErrorSnackBar("Please enter Start time")
                false
            }
            TextUtils.isEmpty(time2)->{
                showErrorSnackBar("Please enter expected End time")
                false
            }else->{
                true
            }

        }
    }


}