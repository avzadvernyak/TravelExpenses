package m.kampukter.travelexpenses.data

import com.tickaroo.tikxml.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class CBRDateConverter : TypeConverter<Date> {

    override fun read(value: String): Date? = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(value)

    override fun write(value: Date): String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(value)
}
class CBRFloatConverter : TypeConverter<Float> {

    override fun read(value: String): Float? = value.replace(",",".").toFloat()

    override fun write(value: Float): String = value.toString()
}
class CBRIntConverter : TypeConverter<Int> {

    override fun read(value: String): Int? = value.toInt()

    override fun write(value: Int): String = value.toString()
}