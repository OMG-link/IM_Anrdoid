package com.omg_link.im.message

import android.view.View
import android.widget.TextView
import com.omg_link.im.MainActivity
import com.omg_link.im.R
import java.security.InvalidParameterException
import java.util.*
import java.util.logging.Level

class TimeMessage(stamp: Long) : Message("System", stamp) {

    override val type: MessageType = MessageType.SYSTEM

    override val infoBarVisibility = View.GONE

    override fun onDataUpdated(holder: MessagePanelHolder) {
        super.onDataUpdated(holder)

        val view = holder.createLayoutFromXML(R.layout.message_time)
        view.findViewById<TextView>(R.id.tvTime).text = stampToString(holder, stamp)
        holder.addView(view)

    }

    companion object {
        fun stampToString(holder: MessagePanelHolder, targetStamp: Long): String {
            val currentStamp = System.currentTimeMillis()

            val currentDate = Calendar.getInstance()
            currentDate.timeInMillis = currentStamp
            val targetDate = Calendar.getInstance()
            targetDate.timeInMillis = targetStamp

            return String.format(
                //format string
                holder.getString(
                    when (getDeltaDay(targetStamp, currentStamp)) {
                        0L -> {
                            R.string.time_today
                        }
                        1L -> {
                            R.string.time_yesterday
                        }
                        in 2L until 6L -> {
                            R.string.time_last_week
                        }
                        else -> {
                            if (currentDate.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR)) {
                                R.string.time_this_year
                            } else {
                                R.string.time_default
                            }
                        }
                    }
                ),
                //时间段
                holder.getString(
                    when (targetDate.get(Calendar.HOUR_OF_DAY)) {
                        in 0..5 -> R.string.time_midnight
                        in 6..10 -> R.string.time_morning
                        in 11..12 -> R.string.time_noon
                        in 13..16 -> R.string.time_afternoon
                        in 17..18 -> R.string.time_dusk
                        in 19..24 -> R.string.time_night
                        else -> throw InvalidParameterException()
                    }
                ),
                //AM/PM
                when (targetDate.get(Calendar.AM_PM)) {
                    Calendar.AM -> "AM"
                    Calendar.PM -> "PM"
                    else -> throw InvalidParameterException()
                },
                //Year
                targetDate.get(Calendar.YEAR),
                //Month
                targetDate.get(Calendar.MONTH),
                //Day
                targetDate.get(Calendar.DAY_OF_MONTH),
                //Hour
                if (targetDate.get(Calendar.HOUR) == 0) {
                    12
                } else {
                    targetDate.get(Calendar.HOUR)
                },
                //Minute
                targetDate.get(Calendar.MINUTE),
                //Day of week
                holder.getString(
                    when (targetDate.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.MONDAY -> R.string.time_monday
                        Calendar.TUESDAY -> R.string.time_tuesday
                        Calendar.WEDNESDAY -> R.string.time_wednesday
                        Calendar.THURSDAY -> R.string.time_thursday
                        Calendar.FRIDAY -> R.string.time_friday
                        Calendar.SATURDAY -> R.string.time_saturday
                        Calendar.SUNDAY -> R.string.time_sunday
                        else -> throw InvalidParameterException()
                    }
                )
            )
        }

        private fun getDeltaDay(stampBegin: Long, stampEnd: Long): Long {
            val timeZone = TimeZone.getDefault()
            val dayBegin = (stampBegin + timeZone.rawOffset) / (24 * 60 * 60 * 1000)
            val dayEnd = (stampEnd + timeZone.rawOffset) / (24 * 60 * 60 * 1000)
            return dayEnd - dayBegin;
        }

    }

}