package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.common.SubmissionTypes.Project
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.projections.SimpleSubmission
import ac.uk.ebi.biostd.persistence.projections.SimpleSubmission.Companion.SIMPLE_GRAPH
import ac.uk.ebi.biostd.persistence.projections.SimpleSubmission.Companion.asSimpleSubmission
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs.named

class ProjectRepository(private val submissionRepository: SubmissionDataRepository) {
    fun findProjectsByAccessTags(tags: List<String>): List<SimpleSubmission> =
        if (tags.isEmpty()) emptyList() else getProjectsByAccessTags(tags)

    private fun getProjectsByAccessTags(tags: List<String>): List<SimpleSubmission> =
        submissionRepository
            .findByRootSectionTypeAndAccNoInAndVersionGreaterThan(
                Project.value, tags, named(SIMPLE_GRAPH))
            .map { it.asSimpleSubmission() }
}
