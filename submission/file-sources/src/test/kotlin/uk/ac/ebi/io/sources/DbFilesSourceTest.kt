package uk.ac.ebi.io.sources

import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.constants.FileFields.DB_ID
import ebi.ac.uk.model.constants.FileFields.DB_MD5
import ebi.ac.uk.model.constants.FileFields.DB_PATH
import ebi.ac.uk.model.constants.FileFields.DB_PUBLISHED
import ebi.ac.uk.model.constants.FileFields.DB_SIZE
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DbFilesSourceTest {
    @Test
    fun whenMissingMd5() =
        runTest {
            val exception =
                assertThrows<IllegalArgumentException> {
                    testWhenFields(
                        dbSize = "10",
                        dbId = "abc",
                        dbPath = "path",
                        dbPublished = false,
                    )
                }
            assertThat(exception).hasMessage(
                "All bypass attributes [md5, size, id, path, published] " +
                    "need to be present or none, found [null, 10, abc, path, false]",
            )
        }

    @Test
    fun whenOnlyMd5() =
        runTest {
            assertThat(testWhenFields(dbMd5 = "abcMd5")).isNull()
        }

    @Test
    fun whenMissingDbSize() =
        runTest {
            val exception =
                assertThrows<IllegalArgumentException> {
                    testWhenFields(
                        dbMd5 = "dbMd5",
                        dbId = "abc",
                        dbPath = "path",
                        dbPublished = false,
                    )
                }
            assertThat(exception).hasMessage(
                "All bypass attributes [md5, size, id, path, published] " +
                    "need to be present or none, found [dbMd5, null, abc, path, false]",
            )
        }

    @Test
    fun whenMissingDbId() =
        runTest {
            val exception =
                assertThrows<IllegalArgumentException> {
                    testWhenFields(
                        dbSize = "10",
                        dbMd5 = "dbMd5",
                        dbPath = "path",
                        dbPublished = false,
                    )
                }
            assertThat(exception).hasMessage(
                "All bypass attributes [md5, size, id, path, published] " +
                    "need to be present or none, found [dbMd5, 10, null, path, false]",
            )
        }

    @Nested
    inner class PathCases {
        @Test
        fun whenMissingPath() =
            runTest {
                val exception =
                    assertThrows<IllegalArgumentException> {
                        testWhenFields(
                            dbId = "abc",
                            dbSize = "10",
                            dbMd5 = "dbMd5",
                            dbPublished = false,
                        )
                    }
                assertThat(exception).hasMessage(
                    "All bypass attributes [md5, size, id, path, published] " +
                        "need to be present or none, found [dbMd5, 10, abc, null, false]",
                )
            }

        @Test
        fun whenRelativePath() =
            runTest {
                val exception =
                    assertThrows<IllegalArgumentException> {
                        testWhenFields(
                            dbId = "abc",
                            dbSize = "10",
                            dbPath = "/path",
                            dbMd5 = "dbMd5",
                            dbPublished = false,
                        )
                    }

                assertThat(exception).hasMessage("Db path '/path' needs to be relative.")
            }
    }

    @Test
    fun whenMissingPublished() =
        runTest {
            val exception =
                assertThrows<IllegalArgumentException> {
                    testWhenFields(
                        dbId = "abc",
                        dbSize = "10",
                        dbMd5 = "dbMd5",
                        dbPath = "path",
                    )
                }

            assertThat(exception).hasMessage(
                "All bypass attributes [md5, size, id, path, published] " +
                    "need to be present or none, found [dbMd5, 10, abc, path, null]",
            )
        }

    private suspend fun testWhenFields(
        dbMd5: String? = null,
        dbSize: String? = null,
        dbId: String? = null,
        dbPath: String? = null,
        dbPublished: Boolean? = null,
    ): ExtFile? {
        val attributes =
            buildList {
                if (dbMd5 != null) add(ExtAttribute(name = DB_MD5.value, value = dbMd5))
                if (dbSize != null) add(ExtAttribute(name = DB_SIZE.value, value = dbSize))
                if (dbId != null) add(ExtAttribute(name = DB_ID.value, value = dbId))
                if (dbPath != null) add(ExtAttribute(name = DB_PATH.value, value = dbPath))
                if (dbPublished != null) add(ExtAttribute(name = DB_PUBLISHED.value, value = dbPublished.toString()))
            }
        return DbFilesSource.getExtFile(
            "path",
            "type",
            attributes,
        )
    }
}
