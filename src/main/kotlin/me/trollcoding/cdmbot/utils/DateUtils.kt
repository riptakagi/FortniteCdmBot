package me.trollcoding.cdmbotsample.utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun dateTimeFormatByDate(date: Date): String {
        return SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)
    }

    fun toStringByTimestamp(timestamp: Timestamp): String {
        val date = Date(timestamp.time)
        return dateTimeFormatByDate(date)
    }
}