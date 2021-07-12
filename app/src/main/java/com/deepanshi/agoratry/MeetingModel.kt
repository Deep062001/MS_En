package com.deepanshi.agoratry

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MeetingModel(
    var name:String,
    var channel_name:String,
    var date:Long,
    var time_from:Long,
    var time_till:Long,
    var isFinished:Int=0,
    @PrimaryKey(autoGenerate = true)
    var id:Long=0
)

