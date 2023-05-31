package ac.uk.ebi.biostd.submission.helpers

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FileFields.DB_ID
import ebi.ac.uk.model.constants.FileFields.DB_MD5
import ebi.ac.uk.model.constants.FileFields.DB_PATH
import ebi.ac.uk.model.constants.FileFields.DB_PUBLISHED
import ebi.ac.uk.model.constants.FileFields.DB_SIZE
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DbFilesSourceTest {

    @Test
    fun whenMissingMd5() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy {
                testWhenFields(
                    dbSize = "10",
                    dbId = "abc",
                    dbPath = "path",
                    dbPublished = false
                )
            }
            .withMessage(
                "All bypass attributes [md5, size, id, path, published] " +
                    "need to be present or none, found [null, 10, abc, path, false]"
            )
    }

    @Test
    fun whenMissingDbSize() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy {
                testWhenFields(
                    dbMd5 = "dbMd5",
                    dbId = "abc",
                    dbPath = "path",
                    dbPublished = false
                )
            }
            .withMessage(
                "All bypass attributes [md5, size, id, path, published] " +
                    "need to be present or none, found [dbMd5, null, abc, path, false]"
            )
    }

    @Test
    fun whenMissingDbId() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy {
                testWhenFields(
                    dbSize = "10",
                    dbMd5 = "dbMd5",
                    dbPath = "path",
                    dbPublished = false
                )
            }
            .withMessage(
                "All bypass attributes [md5, size, id, path, published] " +
                    "need to be present or none, found [dbMd5, 10, null, path, false]"
            )
    }

    @Nested
    inner class PathCases {
        @Test
        fun whenMissingPath() {
            assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy {
                    testWhenFields(
                        dbId = "abc",
                        dbSize = "10",
                        dbMd5 = "dbMd5",
                        dbPublished = false
                    )
                }
                .withMessage(
                    "All bypass attributes [md5, size, id, path, published] " +
                        "need to be present or none, found [dbMd5, 10, abc, null, false]"
                )
        }

        @Test
        fun whenRelativePath() {
            assertThatExceptionOfType(IllegalArgumentException::class.java)
                .isThrownBy {
                    testWhenFields(
                        dbId = "abc",
                        dbSize = "10",
                        dbPath = "/path",
                        dbMd5 = "dbMd5",
                        dbPublished = false
                    )
                }
                .withMessage("Db path '/path' needs to be relative.")
        }
    }

    @Test
    fun whenMissingPublished() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy {
                testWhenFields(
                    dbId = "abc",
                    dbSize = "10",
                    dbMd5 = "dbMd5",
                    dbPath = "path",
                )
            }
            .withMessage(
                "All bypass attributes [md5, size, id, path, published] " +
                    "need to be present or none, found [dbMd5, 10, abc, path, null]"
            )
    }

    private fun testWhenFields(
        dbMd5: String? = null,
        dbSize: String? = null,
        dbId: String? = null,
        dbPath: String? = null,
        dbPublished: Boolean? = null,
    ): ExtFile? {
        val attributes = buildList {
            if (dbMd5 != null) add(Attribute(name = DB_MD5.value, value = dbMd5))
            if (dbSize != null) add(Attribute(name = DB_SIZE.value, value = dbSize))
            if (dbId != null) add(Attribute(name = DB_ID.value, value = dbId))
            if (dbPath != null) add(Attribute(name = DB_PATH.value, value = dbPath))
            if (dbPublished != null) add(Attribute(name = DB_PUBLISHED.value, value = dbPublished.toString()))
        }
        return DbFilesSource.getExtFile(
            "path",
            "type",
            attributes
        )
    }
}
