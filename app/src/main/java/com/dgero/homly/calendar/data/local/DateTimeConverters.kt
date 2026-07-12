package com.dgero.homly.calendar.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

class DateTimeConverters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): Int? = time?.toSecondOfDay()

    @TypeConverter
    fun toLocalTime(secondOfDay: Int?): LocalTime? = secondOfDay?.let { LocalTime.ofSecondOfDay(it.toLong()) }
}
