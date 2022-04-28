package ebi.ac.uk.model.constants

private const val ACC_NO_FIELD = "accno"
private const val SECTION_FIELD = "section"
private const val ATTRIBUTES_FIELD = "attributes"

val SUBMISSION_RESERVED_ATTRIBUTES = setOf(
    SubFields.RELEASE_DATE.value,
    SubFields.TITLE.value,
    SubFields.ATTACH_TO.value,
    SubFields.ON_BEHALF.value,
    SubFields.ROOT_PATH.value
)

val SECTION_RESERVED_ATTRS = setOf(SectionFields.FILE_LIST.value)
val FILES_RESERVED_ATTRS = setOf(FileFields.MD5.value)

interface Fields {
    val value: String?
}

enum class OtherFields(override val value: String) : Fields {
    TABLE("table");

    override fun toString(): String {
        return value
    }
}

enum class SubFields(override val value: String) : Fields {
    SUBMISSION("submission"),
    TYPE("type"),
    ACC_NO(ACC_NO_FIELD),
    ATTRIBUTES(ATTRIBUTES_FIELD),
    SECTION(SECTION_FIELD),
    TITLE("Title"),
    ROOT_PATH("RootPath"),
    PUBLIC_ACCESS_TAG("Public"),

    RELEASE_DATE("ReleaseDate"),
    RELEASE_TIME("rtime"),
    CREATION_TIME("ctime"),
    MODIFICATION_TIME("mtime"),
    SECRET("secretKey"),
    ATTACH_TO("AttachTo"),
    ON_BEHALF("onBehalf"),
    ACC_NO_TEMPLATE("AccNoTemplate"),
    COLLECTION_VALIDATOR("CollectionValidator");

    override fun toString(): String {
        return value
    }
}

enum class SectionFields(override val value: String) : Fields {
    ACC_NO(ACC_NO_FIELD),
    ATTRIBUTES(ATTRIBUTES_FIELD),
    SECTION(SECTION_FIELD),
    TITLE("Title"),
    LINKS("links"),
    SUBSECTIONS("subsections"),
    TYPE("type"),
    FILES("files"),
    FILE_LIST("File List");

    override fun toString(): String {
        return value
    }
}

enum class AttributeFields(override val value: String) : Fields {
    ATTRIBUTE("attribute"),
    NAME("name"),
    VALUE("value"),
    REFERENCE("reference");

    override fun toString(): String {
        return value
    }
}

enum class AttributeDetails(override val value: String) : Fields {
    VAL_QUALIFIER("valqual"),
    NAME_QUALIFIER("nmqual"),

    NAME("name"),
    VALUE("value");

    override fun toString(): String {
        return value
    }
}

enum class LinkFields(override val value: String) : Fields {
    LINK("link"),
    ATTRIBUTES(ATTRIBUTES_FIELD),
    URL("url");

    override fun toString(): String {
        return value
    }
}

enum class FileFields(override val value: String) : Fields {
    FILE("file"),
    NAME("name"),
    PATH("path"),
    SIZE("size"),
    TYPE("type"),
    FILE_TYPE("file"),
    DIR_TYPE("directory"),
    MD5("md5"),
    ATTRIBUTES(ATTRIBUTES_FIELD);

    override fun toString(): String {
        return value
    }
}

enum class TableFields(override val value: String) : Fields {
    LINKS_TABLE("Links"),
    FILES_TABLE("Files");

    override fun toString(): String {
        return value
    }
}
