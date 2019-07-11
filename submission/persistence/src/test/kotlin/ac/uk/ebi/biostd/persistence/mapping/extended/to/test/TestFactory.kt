package ac.uk.ebi.biostd.persistence.mapping.extended.to.test

import ac.uk.ebi.biostd.persistence.model.Attribute
import ac.uk.ebi.biostd.persistence.model.AttributeDetail
import ac.uk.ebi.biostd.persistence.model.File
import ac.uk.ebi.biostd.persistence.model.FileAttribute
import ac.uk.ebi.biostd.persistence.model.Link
import ac.uk.ebi.biostd.persistence.model.LinkAttribute
import ac.uk.ebi.biostd.persistence.model.ReferencedFileAttribute
import ac.uk.ebi.biostd.persistence.model.SectionAttribute

internal val fileAttribute get() = FileAttribute(attribute)
internal val refAttribute get() = ReferencedFileAttribute(attribute)
internal val linkAttribute get() = LinkAttribute(attribute)
internal val sectionAttribute get() = SectionAttribute(attribute)

internal val attribute get() = Attribute("Attribute Name", "Attribute Value", 1, true, mutableListOf(nameAttribute), mutableListOf(valueAttribute))
internal val nameAttribute get() = AttributeDetail(name = "Name Attribute Name", value = "Name Attribute Value")
internal val valueAttribute get() = AttributeDetail(name = "Value Attribute Name", value = "Value Attribute Value")

internal val simpleFile get() = File("fileName", 1, 55L, sortedSetOf(fileAttribute))
internal val simpleLink get() = Link("linkUrl", 1, sortedSetOf(linkAttribute))
