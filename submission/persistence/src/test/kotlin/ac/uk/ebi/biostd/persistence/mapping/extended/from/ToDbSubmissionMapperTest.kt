package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.Tag
import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.test.assertSubmission
import ac.uk.ebi.biostd.persistence.test.extAccessTag
import ac.uk.ebi.biostd.persistence.test.extSubmission
import ac.uk.ebi.biostd.persistence.test.extTag
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val submitter = "user@ebi.email.com"

@ExtendWith(MockKExtension::class)
internal class ToDbSubmissionMapperTest(
    @MockK private val accessTagsRepository: AccessTagDataRepo,
    @MockK private val tagsRepository: TagDataRepository,
    @MockK private var userRepository: UserDataRepository
) {
    private val testInstance = ToDbSubmissionMapper(accessTagsRepository, tagsRepository, userRepository)

    @Test
    fun toSubmissionDb(@MockK accessTag: AccessTag, @MockK tag: Tag, @MockK user: User) {
        every { accessTagsRepository.findByName(extAccessTag.name) } returns accessTag
        every { tagsRepository.findByClassifierAndName(extTag.name, extTag.value) } returns tag
        every { userRepository.getByEmail(submitter) } returns user

        val dbSubmission = testInstance.toSubmissionDb(extSubmission, submitter)

        assertSubmission(dbSubmission, listOf(accessTag), listOf(tag), user)
    }
}
