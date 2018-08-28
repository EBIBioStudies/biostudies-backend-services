package ac.uk.ebi.biostd.serialization.common

import arrow.core.Either
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class EitherSerializer : StdSerializer<Either<*, *>>(Either::class.java) {

    override fun serialize(either: Either<*, *>?, gen: JsonGenerator?, provider: SerializerProvider?) {
        either?.fold({ gen?.writeObject(it) }, { gen?.writeObject(it) })
    }
}
