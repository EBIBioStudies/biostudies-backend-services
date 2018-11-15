package ac.uk.ebi.biostd.persistence.repositories

import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.FULL_DATA_GRAPH
import ac.uk.ebi.biostd.persistence.model.Submission
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface SubmissionDataRepository : JpaRepository<Submission, Long> {

    @EntityGraph(value = FULL_DATA_GRAPH, type = LOAD)
    fun getByAccNoAndVersionGreaterThan(id: String, long: Int = 0): Submission

    fun findByAccNoAndVersionGreaterThan(id: String, long: Int = 0): Optional<Submission>
}

interface TagsDataRepository : JpaRepository<AccessTag, Long> {

    fun findByName(name: String): AccessTag
}

interface IdGen : JpaRepository<IdGen, Long> {

    @Modifying
    @Query("")
    fun getAndIncrement(@Param("prefix") prefix: String, @Param("postfix") postfix: String): Long
}