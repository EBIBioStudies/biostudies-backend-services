package ebi.ac.uk.model.constans


enum class OtherFields(val value: String) {
    TABLE("table");

    override fun toString(): String {
        return value
    }
}

private const val ACC_NO_FIELD = "accNo"
private const val ACCESS_FIELD = "accessTags"
private const val SECTION_FIELD = "section"
private const val ATTRIBUTES_FIELD = "attributes"

enum class SubFields(val value: String) {
    SUBMISSION("submission"),
    REL_PATH("relPath"),
    ACC_NO(ACC_NO_FIELD),
    ACCESS_TAGS(ACCESS_FIELD),
    ATTRIBUTES(ATTRIBUTES_FIELD),
    SECTION(SECTION_FIELD),
    TITLE("Title"),
    ROOT_PATH("RootPath"),
    RELEASE_DATE("ReleaseDate"),
    PUBLIC_ACCESS_TAG("Public"),

    RELEASE_TIME("rtime"),
    CREATION_TIME("rtime"),
    MODIFICATION_TIME("rtime"),
    SECRET("secretKey");

    override fun toString(): String {
        return value
    }
}

enum class SectionFields(val value: String) {
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

enum class AttributeFields(val value: String) {
    ATTRIBUTE("attribute"),
    NAME("name"),
    VALUE("value"),
    TERM("valqual"),
    REFERENCE("reference");

    override fun toString(): String {
        return value
    }
}

enum class LinkFields(val value: String) {
    LINK("link"),
    ATTRIBUTES(ATTRIBUTES_FIELD),
    URL("url");

    override fun toString(): String {
        return value
    }
}

enum class FileFields(val value: String) {
    FILE("file"),
    NAME("name"),
    SIZE("size"),
    TYPE("type"),
    ATTRIBUTES(ATTRIBUTES_FIELD);

    override fun toString(): String {
        return value
    }
}

enum class TableFields(val value: String) {
    LINKS_TABLE("Links"),
    FILES_TABLE("Files");

    override fun toString(): String {
        return value
    }
}