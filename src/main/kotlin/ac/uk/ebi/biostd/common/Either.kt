package ac.uk.ebi.biostd.common

sealed class Either<out L, out R>

data class Left<out T>(val value: T) : Either<T, Nothing>()
data class Right<out T>(val value: T) : Either<Nothing, T>()

inline fun <L, R, T> Either<L, R>.fold(left: (L) -> T, right: (R) -> T): T =
        when (this) {
            is Left -> left(value)
            is Right -> right(value)
        }
