package ebi.ac.uk.model

import ebi.ac.uk.base.EMPTY

class AccPattern(val prefix: String = EMPTY, val postfix: String = EMPTY) {

    override fun toString() = "$prefix,$postfix"
}
