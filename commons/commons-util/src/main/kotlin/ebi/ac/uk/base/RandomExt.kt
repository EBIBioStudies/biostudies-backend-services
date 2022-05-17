package ebi.ac.uk.base

import kotlin.random.Random

fun Random.roll(probability: Int): Boolean {
    require(probability in 1..99) { "Probability need to be a value in the range 1-99  " }
    return Random.nextInt(0, 100 / probability) == 0
}
