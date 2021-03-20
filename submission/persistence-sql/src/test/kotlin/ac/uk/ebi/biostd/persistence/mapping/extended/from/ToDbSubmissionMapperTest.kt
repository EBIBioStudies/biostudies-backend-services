package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.test.OWNER
import ac.uk.ebi.biostd.persistence.test.SUBMITTER
import ac.uk.ebi.biostd.persistence.test.assertSubmission
import ac.uk.ebi.biostd.persistence.test.extCollection
import ac.uk.ebi.biostd.persistence.test.extSubmission
import ac.uk.ebi.biostd.persistence.test.extTag
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class ToDbSubmissionMapperTest(
    @MockK private val accessTagsRepository: AccessTagDataRepo,
    @MockK private val tagsRepository: TagDataRepository,
    @MockK private var userRepository: UserDataRepository
) {
    private val testInstance = ToDbSubmissionMapper(accessTagsRepository, tagsRepository, userRepository)

    @Test
    fun `to db submission`(
        @MockK accessTag: DbAccessTag,
        @MockK tag: DbTag,
        @MockK user: DbUser,
        @MockK submitter: DbUser
    ) {
        every { accessTagsRepository.findByName(extCollection.accNo) } returns accessTag
        every { tagsRepository.findByClassifierAndName(extTag.name, extTag.value) } returns tag
        every { userRepository.findByEmail(OWNER) } returns Optional.of(user)
        every { userRepository.findByEmail(SUBMITTER) } returns Optional.of(submitter)

        val dbSubmission = testInstance.toSubmissionDb(extSubmission)

        assertSubmission(dbSubmission, listOf(accessTag), listOf(tag), user, submitter)
    }

    @Test
    fun `non existing owner`(
        @MockK user: DbUser,
        @MockK submitter: DbUser
    ) {
        every { userRepository.findByEmail(OWNER) } returns Optional.empty()
        every { userRepository.findByEmail(SUBMITTER) } returns Optional.of(submitter)

        val exception = assertThrows<UserNotFoundException> { testInstance.toSubmissionDb(extSubmission) }
        assertThat(exception.message).isEqualTo("The user with email '$OWNER' could not be found")
    }

    @Test
    fun `non existing submitter`(
        @MockK user: DbUser,
        @MockK submitter: DbUser
    ) {
        every { userRepository.findByEmail(OWNER) } returns Optional.of(user)
        every { userRepository.findByEmail(SUBMITTER) } returns Optional.empty()

        val exception = assertThrows<UserNotFoundException> { testInstance.toSubmissionDb(extSubmission) }
        assertThat(exception.message).isEqualTo("The user with email '$SUBMITTER' could not be found")
    }
}
