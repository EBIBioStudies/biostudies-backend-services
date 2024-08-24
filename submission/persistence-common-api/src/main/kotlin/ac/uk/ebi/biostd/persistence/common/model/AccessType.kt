package ac.uk.ebi.biostd.persistence.common.model

enum class AccessType {
    READ,
    ATTACH,
    UPDATE,
    DELETE,
    DELETE_FILES,
    ADMIN,
}

interface AccessPermission {
    val accessType: AccessType
    val accessTag: AccessTag
}
