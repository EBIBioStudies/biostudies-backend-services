package ebi.ac.uk.asserts

import arrow.core.Either
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import org.assertj.core.api.Assertions.assertThat

fun assertSubmission(submission: Submission, accNo: String, title: String, vararg attributes: Attribute) {
    assertThat(submission.accNo).isEqualTo(accNo)
    assertThat(submission.title).isEqualTo(title)
    assertAttributes(submission.attributes, attributes)
}

fun assertSection(
    section: Either<Section, SectionsTable>,
    expectedAccNo: String?,
    expectedType: String,
    vararg expectedAttributes: Attribute
) = section.ifLeft { performSectionAssertion(it, expectedAccNo, expectedType, expectedAttributes) }

fun assertSection(section: Section, expectedAccNo: String?, expectedType: String, vararg expectedAttributes: Attribute) =
    performSectionAssertion(section, expectedAccNo, expectedType, expectedAttributes)

fun assertSectionsTable(sectionsTable: Either<Section, SectionsTable>, vararg expectedRowSections: Section) {
    sectionsTable.ifRight {
        assertThat(it.elements).hasSize(expectedRowSections.size)
        it.elements.forEachIndexed{ index, rowSection ->
            val expected = expectedRowSections[index]
            performSectionAssertion(rowSection, expected.accNo, expected.type, expected.attributes.toTypedArray())
        }
    }
}

fun assertLink(link: Either<Link, LinksTable>, expectedUrl: String, vararg expectedAttributes: Attribute) =
    link.ifLeft { performLinkAssertion(it, expectedUrl, expectedAttributes) }

// TODO table assertion abstraction
// TODO reuse in TsvDeserializerTest
fun assertLinksTable(linksTable: Either<Link, LinksTable>, vararg expectedRowLinks: Link) {
    linksTable.ifRight {
        assertThat(it.elements).hasSize(expectedRowLinks.size)
        it.elements.forEachIndexed{ index, rowLink ->
            val expected = expectedRowLinks[index]
            performLinkAssertion(rowLink, expected.url, expected.attributes.toTypedArray())
        }
    }
}

private fun performSectionAssertion(
    section: Section,
    expectedAccNo: String?,
    expectedType: String,
    expectedAttributes: Array<out Attribute>
) {
    assertThat(section.accNo).isEqualTo(expectedAccNo)
    assertThat(section.type).isEqualTo(expectedType)
    assertAttributes(section.attributes, expectedAttributes)
}

private fun performLinkAssertion(link: Link, expectedUrl: String, expectedAttributes: Array<out Attribute>) {
    assertThat(link.url).isEqualTo(expectedUrl)
    assertAttributes(link.attributes, expectedAttributes)
}
