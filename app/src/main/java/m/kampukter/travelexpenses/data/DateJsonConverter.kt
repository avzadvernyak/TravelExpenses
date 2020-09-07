package m.kampukter.travelexpenses.data

import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class DateJsonConverter: JsonSerializer<Date>, JsonDeserializer<Date> {
    override fun serialize(
        src: Date,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement = JsonPrimitive(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).format(src))

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Date? = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).parse(json.asString)
}