package ac.uk.ebi.biostd.persistence.common.model

enum class AccessType {
    READ, ATTACH, UPDATE, DELETE
}

interface AccessPermission {
    val accessType: AccessType
    val accessTag: AccessTag
}
