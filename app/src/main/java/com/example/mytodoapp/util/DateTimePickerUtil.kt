package com.example.mytodoapp.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimePickerUtil {

    fun pickDateTime(context: Context, initialTime: Long? = null, onPicked: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        if (initialTime != null && initialTime > 0) {
            calendar.timeInMillis = initialTime
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val selected = Calendar.getInstance()
                        selected.set(year, month, day, hour, minute, 0)
                        selected.set(Calendar.MILLISECOND, 0)
                        onPicked(selected.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatTimeRange(startMillis: Long?, endMillis: Long?): String {
        if (startMillis == null || startMillis <= 0) return ""
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        val startStr = timeFormat.format(Date(startMillis))
        val endStr = if (endMillis != null && endMillis > 0) timeFormat.format(Date(endMillis)) else ""
        val dateStr = dateFormat.format(Date(startMillis))
        return if (endStr.isNotEmpty()) {
            "$dateStr, $startStr – $endStr"
        } else {
            "$dateStr, $startStr"
        }
    }
}