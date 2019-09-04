@file:Suppress("TooManyFunctions")

package ebi.ac.uk.dsl

import ebi.ac.uk.model.Attributable
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Attributes
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission

fun Attributable.attribute(
    name: String,
    value: String,
    ref: Boolean = false,
    valueAttrs: Attributes = mutableListOf(),
    nameAttrs: Attributes = mutableListOf()
) =
    addAttribute(Attribute(name = name, value = value, valueAttrs = valueAttrs, reference = ref, nameAttrs = nameAttrs))

fun Submission.section(type: String, block: Section.() -> Unit) = apply { section = Section(type).apply(block) }
fun submission(accNo: String, block: Submission.() -> Unit): Submission = Submission(accNo).apply(block)
fun section(type: String, block: Section.() -> Unit): Section = Section(type).apply(block)
fun sectionsTable(block: SectionsTable.() -> Unit) = SectionsTable().apply(block)

fun FilesTable.file(name: String, block: File.() -> Unit = {}) = addRow(File(name).apply(block))
fun SectionsTable.section(type: String, block: Section.() -> Unit = {}) = addRow(Section(type).apply(block))
fun LinksTable.link(url: String, block: Link.() -> Unit = {}) = addRow(Link(url).apply(block))

fun Section.filesTable(block: FilesTable.() -> Unit) = addFilesTable(FilesTable().apply(block))
fun Section.sectionsTable(block: SectionsTable.() -> Unit) = addSectionTable(SectionsTable().apply(block))
fun Section.linksTable(block: LinksTable.() -> Unit) = addLinksTable(LinksTable().apply(block))

fun Section.file(name: String, block: File.() -> Unit = {}) = addFile((File(name).apply(block)))
fun Section.section(type: String, block: Section.() -> Unit) = addSection((Section(type).apply(block)))
fun Section.link(url: String, block: Link.() -> Unit = {}) = addLink((Link(url).apply(block)))

fun link(url: String, block: Link.() -> Unit = {}) = Link(url).apply(block)
fun linksTable(block: LinksTable.() -> Unit) = LinksTable().apply(block)

fun file(path: String, block: File.() -> Unit = {}) = File(path).apply(block)
fun filesTable(block: FilesTable.() -> Unit) = FilesTable().apply(block)
