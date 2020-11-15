package ac.uk.ebi.biostd.persistence.repositories.data

import ac.uk.ebi.biostd.persistence.common.SubmissionTypes.Project
import ac.uk.ebi.biostd.persistence.common.model.SimpleSubmission
import ac.uk.ebi.biostd.persistence.common.service.ProjectDataService
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.SIMPLE_QUERY_GRAPH
import ac.uk.ebi.biostd.persistence.model.ext.title
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs.named

internal class ProjectSqlDataService(private val submissionRepository: SubmissionDataRepository) : ProjectDataService {

    override fun findProjectsByAccessTags(tags: List<String>): List<SimpleSubmission> =
        if (tags.isEmpty()) emptyList() else getProjectsByAccessTags(tags)

    private fun getProjectsByAccessTags(tags: List<String>): List<SimpleSubmission> =
        submissionRepository
            .findByRootSectionTypeAndAccNoInAndVersionGreaterThan(
                Project.value, tags, named(SIMPLE_GRAPH))
            .map { it.asSimpleSubmission() }

    companion object {
        const val SIMPLE_GRAPH: String = SIMPLE_QUERY_GRAPH

        fun DbSubmission.asSimpleSubmission(): SimpleSubmission =
            SimpleSubmission(
                accNo = accNo,
                version = version,
                secretKey = secretKey,
                title = title ?: rootSection.title,
                relPath = relPath,
                released = released,
                creationTime = creationTime,
                modificationTime = modificationTime,
                releaseTime = releaseTime,
                status = status,
                method = method)
    }
}
