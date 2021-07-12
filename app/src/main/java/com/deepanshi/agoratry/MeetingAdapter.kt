package com.deepanshi.agoratry


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_meeting.view.*
import java.sql.Date
import java.text.SimpleDateFormat

class MeetingAdapter(val list: List<MeetingModel>):RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        return MeetingViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meeting,parent,false))
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemId(position: Int): Long {
        return list[position].id
    }

    fun getItemchannel(position: Int):String{
        return list[position].channel_name
    }

    override fun getItemCount()=list.size

    class MeetingViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bind(meetingModel: MeetingModel) {
            with(itemView){
                tv_name.text=meetingModel.name
                tv_channel_name.text=meetingModel.channel_name
                updateDate(meetingModel.date)
                tv_time_from.text=updateTime(meetingModel.time_from)
                tv_time_till.text=updateTime(meetingModel.time_till)
            }

        }

        private fun updateTime(time: Long): String {
            val myformat = "h:mm a"
            val sdf=SimpleDateFormat(myformat)
            return sdf.format(Date(time))

        }

        private fun updateDate(time: Long) {
            val myformat = "EEE, d MMM yyyy"
            val sdf = SimpleDateFormat(myformat)
            itemView.tv_Date.text = sdf.format(Date(time))

        }

    }


}