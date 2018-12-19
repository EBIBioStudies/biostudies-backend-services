package ebi.ac.uk.model.constants

private const val ACC_NO_FIELD = "accNo"
private const val ACCESS_FIELD = "accessTags"
private const val SECTION_FIELD = "section"
private const val ATTRIBUTES_FIELD = "attributes"

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
    REL_PATH("relPath"),
    ACC_NO(ACC_NO_FIELD),
    ACCESS_TAGS(ACCESS_FIELD),
    ATTRIBUTES(ATTRIBUTES_FIELD),
    SECTION(SECTION_FIELD),
    TITLE("Title"),
    ROOT_PATH("RootPath"),
    PUBLIC_ACCESS_TAG("Public"),

    RELEASE_TIME("rtime"),
    CREATION_TIME("rtime"),
    MODIFICATION_TIME("rtime"),
    SECRET("secretKey"),
    ATTACH_TO("AttachTo"),
    ACC_NO_TEMPLATE("AccNoTemplate");

    override fun toString(): String {
        return value
    }
}

enum class SectionFields(override val value: String) : Fields {
    ACC_NO(ACC_NO_FIELD),
    ACCESS_TAGS(ACCESS_FIELD),
    ATTRIBUTES(ATTRIBUTES_FIELD),
    SECTION(SECTION_FIELD),
    LINKS("links"),
    SUBSECTIONS("subsections"),
    TYPE("type"),
    FILES("files"),
    PARENT_ACC_NO("parentAccNo");

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
    SIZE("size"),
    TYPE("type"),
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