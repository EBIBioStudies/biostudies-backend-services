package ac.uk.ebi.biostd.persistence.doc.db.converters

import arrow.core.Either
import org.bson.Document
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.convert.MongoConverter

inline fun <reified T, reified V> createToDocConverter(mapper: MongoConverter): Converter<Either<T, V>, Document> {
    return Converter { either ->
        val document = Document()
        either.fold({ mapper.write(it, document) }, { mapper.write(it, document) })
        document
    }
}

inline fun <reified T, reified V> createFromDocConverter(mapper: MongoConverter): Converter<Document, Either<T, V>> {
    return Converter {
        when (val type = it["_class"]) {
            T::class.java.name -> Either.left(mapper.read(T::class.java, it))
            V::class.java.name -> Either.right(mapper.read(V::class.java, it))
            else -> throw IllegalStateException("Invalid class type $type")
        }
    }
}
