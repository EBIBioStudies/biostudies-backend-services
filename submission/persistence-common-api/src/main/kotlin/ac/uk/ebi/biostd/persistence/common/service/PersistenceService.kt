package ac.uk.ebi.biostd.persistence.common.service

interface PersistenceService {
    fun sequenceAccNoPatternExists(pattern: String): Boolean
    fun createAccNoPatternSequence(pattern: String)
    fun getSequenceNextValue(pattern: String): Long
    fun saveAccessTag(accessTag: String)
    fun accessTagExists(accessTag: String): Boolean
}
