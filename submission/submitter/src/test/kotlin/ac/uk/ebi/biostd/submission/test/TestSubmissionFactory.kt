package ac.uk.ebi.biostd.submission.test

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User
import java.nio.file.Paths

const val USER_ID = 123L
const val ACC_NO = "ABC456"
const val USER_EMAIL = "user@mail.com"
const val USER_SECRET_KEY = "SecretKey"

fun createTestUser() = User(USER_ID, USER_EMAIL, USER_SECRET_KEY, Paths.get(""))

fun createBasicExtendedSubmission() = ExtendedSubmission(ACC_NO, createTestUser())
