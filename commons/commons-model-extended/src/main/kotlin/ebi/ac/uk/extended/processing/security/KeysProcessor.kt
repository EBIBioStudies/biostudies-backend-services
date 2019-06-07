package ebi.ac.uk.extended.processing.security

import java.util.UUID

class KeysProcessor {

    fun newSecurityKey() = UUID.randomUUID().toString()
}
