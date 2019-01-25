package ac.uk.ebi.pmc.importer.data

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.pmc.data.MongoDocService
import ac.uk.ebi.pmc.data.MongoRepository
import ac.uk.ebi.pmc.data.docs.SubmissionDoc
import ebi.ac.uk.model.Submission
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(MockKExtension::class)
internal class MongoDocServiceTest(
    @MockK val dataRepository: MongoRepository,
    @MockK val serializationService: SerializationService
) {

    private val testInstance = MongoDocService(dataRepository, serializationService)

    private val subDocSlot = slot<SubmissionDoc>()
    private val exampleFile = File("example_file")
    private val fileId = ObjectId.get()
    private val submission = Submission(accNo = "acc123")
    private val submissionJson = "json {}"

    @Test
    fun `save submission with file`(): Unit = runBlocking {
        coEvery { dataRepository.saveSubFile(exampleFile, submission.accNo) } returns fileId
        coEvery { dataRepository.save(ofType(SubmissionDoc::class)) } returns Unit
        every { serializationService.serializeSubmission(submission, SubFormat.JSON) } returns submissionJson

        testInstance.saveSubmission(submission, "source file", listOf(exampleFile))

        coVerify { dataRepository.save(capture(subDocSlot)) }
        assertSubmission(subDocSlot.captured, submission.accNo, submissionJson, listOf(fileId))
        return@runBlocking
    }

    private fun assertSubmission(doc: SubmissionDoc, id: String, body: String, files: List<ObjectId>) {
        assertThat(doc.id).isEqualTo(id)
        assertThat(doc.body).isEqualTo(body)
        assertThat(doc.files).containsExactlyElementsOf(files)
    }
}
