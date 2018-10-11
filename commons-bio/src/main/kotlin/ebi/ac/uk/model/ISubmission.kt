package ebi.ac.uk.model

import java.time.OffsetDateTime

const val NO_TABLE_INDEX = -1

interface ISubmission : IAttributable {

    var accNo: String
    var title: String
    var relPath: String
    var creationTime: OffsetDateTime
    var releaseTime: OffsetDateTime
    var modificationTime: OffsetDateTime

    var attachTo: String?
    var rootPath: String?

    var rootSection: ISection
    var user: IUser
    var accessTags: MutableSet<IAccessTag>

    val allFiles: List<IFile>
        get() = rootSection.sections.flatMap { it.files }
}

interface IAccessTag {

    var name: String

    fun isPublic(): Boolean
}

interface IUser {
    var id: Long
    var secretKey: String

    val groups: List<IGroup>
}

interface IGroup {
    var id: Long
    var name: String
    var secretKey: String
}

interface ISection : IAttributable, ITabular {

    var accNo: String?
    var type: String

    var sections: MutableSet<ISection>
    var files: MutableSet<IFile>
    var links: MutableSet<ILink>
}

interface IFile : IAttributable, ITabular {

    var name: String
    var size: Int
}

interface ILink : IAttributable, ITabular {
    var url: String
}


interface IAttribute {
    var name: String
    var value: String
    var reference: Boolean

    var nameAttributes: MutableList<ISimpleAttribute>
    var valueAttributes: MutableList<ISimpleAttribute>
}

interface ISimpleAttribute {
    var name: String
    var value: String
}

interface IAttributable {
    var attributes: MutableSet<IAttribute>
}

interface ITabular {

    var tableIndex: Int
    var order: Int
}