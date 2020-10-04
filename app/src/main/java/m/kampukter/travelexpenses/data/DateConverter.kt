package m.kampukter.travelexpenses.data

import android.text.TextUtils
import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class DateConverter {
    @TypeConverter
    fun dateToString(date: Date): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).format(date)

    @TypeConverter
    fun stringToDate(string: String): Date? =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).parse(string)

    @TypeConverter
    fun stringToMyLocation(string: String?): MyLocation? {
        if (TextUtils.isEmpty(string))
            return null
        return Gson().fromJson(string, MyLocation::class.java)
    }

    @TypeConverter
    fun myLocationToString(location: MyLocation?): String? {
        return if (location == null) null
        else Gson().toJson(location)
    }
}