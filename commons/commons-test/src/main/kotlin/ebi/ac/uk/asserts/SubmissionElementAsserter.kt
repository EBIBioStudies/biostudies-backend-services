package ebi.ac.uk.asserts

import arrow.core.Either
import arrow.core.getOrElse

inline fun <reified A, reified B> Either<A, B>.getLeft() =
    swap().getOrElse { throw AssertionError("Expecting either to have left value of type ${A::class.simpleName}") }

inline fun <reified A, reified B> Either<A, B>.getRight() =
    getOrElse { throw AssertionError("Expecting either to have left value of type ${A::class.simpleName}") }
