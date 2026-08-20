package com.example.mytodoapp.util

import java.util.Calendar

object CalendarUtil {

    fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun monthYearLabel(calendar: Calendar): String {
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return "${months[calendar.get(Calendar.MONTH)]} ${calendar.get(Calendar.YEAR)}"
    }

    // Returns a list of 42 cells (6 weeks). Each cell is a Calendar instance for that day,
    // or null if the cell falls outside the visible range at the start (we still fill with
    // real dates from previous/next month to keep the grid simple, marking them separately).
    fun getMonthGrid(base: Calendar): List<Calendar> {
        val firstOfMonth = (base.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstWeekday = firstOfMonth.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

        val gridStart = (firstOfMonth.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, -firstWeekday)
        }

        val cells = mutableListOf<Calendar>()
        val cursor = gridStart.clone() as Calendar
        repeat(42) {
            cells.add(cursor.clone() as Calendar)
            cursor.add(Calendar.DAY_OF_MONTH, 1)
        }
        return cells
    }

    fun isSameMonth(day: Calendar, month: Calendar): Boolean {
        return day.get(Calendar.YEAR) == month.get(Calendar.YEAR) &&
                day.get(Calendar.MONTH) == month.get(Calendar.MONTH)
    }
}