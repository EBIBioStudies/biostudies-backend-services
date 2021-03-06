package ac.uk.ebi.biostd.persistence.repositories.data

import ac.uk.ebi.biostd.persistence.common.SubmissionTypes.Project
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.SIMPLE_QUERY_GRAPH
import ac.uk.ebi.biostd.persistence.model.ext.title
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs.named

internal class CollectionSqlDataService(
    private val submissionRepository: SubmissionDataRepository
) : CollectionDataService {
    override fun findCollectionsByAccessTags(tags: List<String>): List<BasicSubmission> =
        if (tags.isEmpty()) emptyList() else getCollectionsByAccessTags(tags)

    private fun getCollectionsByAccessTags(tags: List<String>): List<BasicSubmission> =
        submissionRepository
            .findByRootSectionTypeAndAccNoInAndVersionGreaterThan(Project.value, tags, named(SIMPLE_GRAPH))
            .map { it.asBasicSubmission() }

    companion object {
        const val SIMPLE_GRAPH: String = SIMPLE_QUERY_GRAPH

        fun DbSubmission.asBasicSubmission(): BasicSubmission =
            BasicSubmission(
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
                method = method,
                owner = owner.email
            )
    }
}
