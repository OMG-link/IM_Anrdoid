package com.omg_link.im.android.message

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.omg_link.im.R
import com.omg_link.im.android.tools.ViewUtils
import java.security.InvalidParameterException
import java.util.*

class TimeMessage(stamp: Long) : Message(stamp) {
    override val isUserMessage = false
    override val type = Type.TIME

    companion object {
        fun stampToString(holder: TimeMessageHolder, targetStamp: Long): String {
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
                targetDate.get(Calendar.MONTH) + 1,
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

class TimeMessageHolder(itemView: View) : MessageHolder(itemView) {

    constructor(context: Context, parent: ViewGroup) : this(createView(context, parent))

    private val tvTime: TextView = itemView.findViewById(R.id.tvMessageTime)

    fun bind(timeMessage: TimeMessage) {
        super.bind(timeMessage as Message)
        tvTime.text = TimeMessage.stampToString(this, timeMessage.stamp)
    }

    companion object {
        fun createView(context: Context, parent: ViewGroup): View {
            val view = ViewUtils.createLayoutFromXML(context, parent, R.layout.message_time)
            return createView(context, parent, listOf(view))
        }
    }

}