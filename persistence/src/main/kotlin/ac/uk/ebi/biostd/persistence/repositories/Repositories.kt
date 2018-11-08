package ac.uk.ebi.biostd.persistence.repositories

import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.FULL_DATA_GRAPH
import ac.uk.ebi.biostd.persistence.model.Submission
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD
import org.springframework.data.jpa.repository.JpaRepository

interface SubmissionRepository : JpaRepository<Submission, Long> {

    @EntityGraph(value = FULL_DATA_GRAPH, type = LOAD)
    fun findByAccNoAndVersionGreaterThan(id: String, long: Int = 0): Submission
}

interface TagsRepository : JpaRepository<AccessTag, Long> {

    fun findByName(name: String): AccessTag
}
