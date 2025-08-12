package ebi.ac.uk.base

sealed class Either<out A, out B> {
    data class Left<out A>(
        val value: A,
    ) : Either<A, Nothing>() {
        val a: A = value
    }

    data class Right<out B>(
        val value: B,
    ) : Either<Nothing, B>() {
        val b: B = value
    }

    inline fun <C> fold(
        leftFunc: (A) -> C,
        rightFunc: (B) -> C,
    ): C =
        when (this) {
            is Left -> leftFunc(value)
            is Right -> rightFunc(value)
        }

    inline fun <C> mapLeft(mapFunc: (A) -> C): Either<C, B> =
        when (this) {
            is Left -> left<C>(mapFunc(value))
            is Right -> this
        }

    inline fun <C> mapRigh(mapFunc: (B) -> C): Either<A, C> =
        when (this) {
            is Left -> this
            is Right -> right<C>(mapFunc(value))
        }

    inline fun <C, D> bimap(
        leftFunc: (A) -> C,
        rightFunc: (B) -> D,
    ): Either<C, D> =
        when (this) {
            is Left -> left<C>(leftFunc(value))
            is Right -> right<D>(rightFunc(value))
        }

    fun isLeft(): Boolean =
        when (this) {
            is Left -> true
            is Right -> false
        }

    fun isRight(): Boolean =
        when (this) {
            is Left -> false
            is Right -> true
        }

    companion object {
        fun <A> left(element: A): Either<A, Nothing> = Left(element)

        fun <B> right(element: B): Either<Nothing, B> = Right(element)
    }
}

inline fun <A, B, C, D> List<Either<A, B>>.biMap(
    leftFunc: (A) -> C,
    rightFunc: (B) -> D,
) = map { it.bimap(leftFunc, rightFunc) }
