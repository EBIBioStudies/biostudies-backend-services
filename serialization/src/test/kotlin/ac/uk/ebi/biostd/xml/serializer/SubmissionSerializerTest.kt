package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.type
import org.junit.Test
import org.redundent.kotlin.xml.xml
import org.xmlunit.assertj.XmlAssert.assertThat

private const val ACC_NO = "ABC-123"
private const val TITLE = "Session Title"
private const val TAG = "Access_Tag"
private const val ROOT_PATH = "path"
private const val RELEASE_DATE = 1537537261L

private const val ATTR_NAME = "attribute_name"
private const val ATTR_VALUE = "attribute_value"

private const val SEC_TYPE = "Study"
private const val SEC_ACC_NO = "SEC-123"

class SubmissionSerializerTest {

    private val testInstance = XmlSerializer()

    private val testSubmission: Submission = submission {
        accNo = ACC_NO
        //    title = TITLE
        //   rtime = RELEASE_DATE
        accessTags = mutableListOf(TAG)
        //  rootPath = ROOT_PATH

        attribute(ATTR_NAME, ATTR_VALUE)

        section {
            type = SEC_TYPE
            accNo = SEC_ACC_NO
        }
    }

    @Test
    fun testSerializeSubmission() {
        val result = testInstance.serialize(testSubmission)
        val expected = xml("submission") {
            attribute("accNo", ACC_NO)
            "attributes" {
                "attribute" {
                    "name" { -ATTR_NAME }
                    "value" { -ATTR_VALUE }
                }
                "attribute" {
                    "name"  { -"Title" }
                    "value" { -TITLE }
                }
                "attribute" {
                    "name"  { -"ReleaseDate" }
                    "value" { -"2018-09-21T13:41:01Z" }
                }
                "attribute" {
                    "name"  { -"RootPath" }
                    "value" { -ROOT_PATH }
                }
            }
            "section" {
                attribute("accNo", SEC_ACC_NO)
                attribute("type", SEC_TYPE)
            }


        }.toString()

        assertThat(result).and(expected).ignoreElementContentWhitespace().ignoreChildNodesOrder().ignoreWhitespace().areIdentical()
    }
}