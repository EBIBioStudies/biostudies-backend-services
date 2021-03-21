package ac.uk.ebi.pmc

import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.LOADED
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.PROCESSED
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Attribute
import java.time.Instant

val body1 = jsonObj {
    "accno" to "S-EPMC1234567"
    "section" to jsonObj {
        "type" to "Study"
        "files" to jsonArray(
            jsonObj { "path" to "file1.txt"; "size" to "10"; "type" to "file" }
        )
        "subsections" to jsonArray(
            jsonObj {
                "type" to "Publication"
                "files" to jsonArray(
                    jsonObj { "path" to "file2.txt"; "size" to "10"; "type" to "file" }
                )
                "attributes" to jsonArray(
                    jsonObj { "name" to "Publication date"; "value" to "2019" }
                )
            }
        )
    }
}

val body2 = jsonObj {
    "accno" to "S-EPMC8901234"
    "section" to jsonObj {
        "type" to "Study"
        "files" to jsonArray(
            jsonObj { "path" to "file3.txt"; "size" to "10"; "type" to "file" }
        )
        "subsections" to jsonArray(
            jsonObj {
                "type" to "Publication"
                "attributes" to jsonArray(
                    jsonObj { "name" to "Publication date"; "value" to "2019" }
                )
            }
        )
    }
}
val body3 = jsonObj {
    "accno" to "S-EPMC1234567"
    "section" to jsonObj {
        "type" to "Study"
        "files" to jsonArray(
            jsonObj { "path" to "file1.txt"; "size" to "10"; "type" to "file" }
        )
        "subsections" to jsonArray(
            jsonObj {
                "type" to "Publication"
                "attributes" to jsonArray(
                    jsonObj { "name" to "Publication date"; "value" to "2019" }
                )
            }
        )
    }
}
val FILE1_CONTENT = "that is the content"
val FILE2_CONTENT = "that is another the content"
val FILE1_PATH = "/1234567/file1.txt"
val FILE2_PATH = "/1234567/file2.txt"
val FILE3_PATH = "/8901234/file3.txt"
val ERROR_ACCNO = "S-EPMC8901234"
val ERROR_SOURCE_FILE = "sourceFile2"

val FILE1_NAME = "file1.txt"
val FILE2_NAME = "file2.txt"
val URL_FILE1_FILES_SERVER = "/files/getFileStream/PMC1234567?filename=file1.txt"
val URL_FILE2_FILES_SERVER = "/files/getFileStream/PMC1234567?filename=file2.txt"
val URL_FILE3_FILES_SERVER = "/files/getFileStream/PMC8901234?filename=file3.txt"
internal val SUB_ERROR_TEXT = "Submission\tS-456ERROR\tPublic\n\nStudy\n\nLinks"
internal val ACC_NO = "S-123SUCCESS"
internal val SUB_ATTRIBUTE = Attribute("Title", "Submission title")

internal val submissionDoc1 = SubmissionDoc(
    accno = "S-123SUCCESS",
    body = body1.toString(),
    status = LOADED,
    sourceFile = "sourceFile1",
    posInFile = 0,
    sourceTime = Instant.parse("2021-03-14T08:41:02Z"),
    files = listOf(),
    updated = Instant.parse("2021-03-14T08:41:45.090Z")
)

internal val submissionDoc2 = SubmissionDoc(
    accno = "S-EPMC8901234",
    body = body2.toString(),
    status = LOADED,
    sourceFile = "sourceFile2",
    posInFile = 1,
    sourceTime = Instant.parse("2021-03-14T08:41:02Z"),
    files = listOf(),
    updated = Instant.parse("2021-03-14T08:41:45.090Z")
)

internal val submissionDoc3 = SubmissionDoc(
    accno = "S-EPMC1234567",
    body = body3.toString(),
    status = PROCESSED,
    sourceFile = "sourceFile1",
    posInFile = 0,
    sourceTime = Instant.parse("2021-03-14T08:41:02Z"),
    files = listOf(),
    updated = Instant.parse("2021-03-14T08:41:45.090Z")
)
