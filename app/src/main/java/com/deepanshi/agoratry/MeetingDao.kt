package com.deepanshi.agoratry

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.deepanshi.agoratry.MeetingModel

@Dao
interface MeetingDao{
    @Insert()
    suspend fun insertMeet(meetingModel: MeetingModel):Long

    //[To get task that aren't finished yet]
    @Query("Select * from MeetingModel where isFinished == 0")
    fun getMeet():LiveData<List<MeetingModel>>

    @Query("Update MeetingModel Set isFinished= 1 where id=:uid")
    fun finishMeet(uid:Long)

    @Query("Delete from MeetingModel where id=:uid")
    fun deleteMeet(uid:Long)

}