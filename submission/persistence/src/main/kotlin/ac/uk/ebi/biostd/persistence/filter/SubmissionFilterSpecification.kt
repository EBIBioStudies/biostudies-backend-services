package ac.uk.ebi.biostd.persistence.filter

import ac.uk.ebi.biostd.persistence.model.DbSection
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.constants.SECTION_TYPE
import ac.uk.ebi.biostd.persistence.model.constants.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.model.constants.SUB_OWNER
import ac.uk.ebi.biostd.persistence.model.constants.SUB_OWNER_ID
import ac.uk.ebi.biostd.persistence.model.constants.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.model.constants.SUB_ROOT_SECTION
import ac.uk.ebi.biostd.persistence.model.constants.SUB_TITLE
import ac.uk.ebi.biostd.persistence.model.constants.SUB_VERSION
import ebi.ac.uk.base.applyIfNotBlank
import org.springframework.data.jpa.domain.Specification
import java.time.OffsetDateTime

class SubmissionFilterSpecification(filter: SubmissionFilter, userId: Long? = null) {
    val specification: Specification<DbSubmission>

    init {
        var specs = where(withActiveVersion() and isLastVersion())
        userId?.let { specs = specs and withUser(it) }
        filter.accNo?.let { specs = specs and (withAccession(it)) }
        filter.keywords?.applyIfNotBlank { specs = specs and (withTitleLike(it)) }
        filter.rTimeTo?.let { specs = specs and (withTo(OffsetDateTime.parse(it))) }
        filter.rTimeFrom?.let { specs = specs and (withFrom(OffsetDateTime.parse(it))) }
        filter.type?.let { specs = specs and (withType(it)) }
        specification = specs
    }

    private fun withActiveVersion(): Specification<DbSubmission> =
        Specification { root, _, cb -> cb.greaterThan<Int>(root.get<Int>(SUB_VERSION), 0) }

    private fun isLastVersion(): Specification<DbSubmission> =
        Specification { root, query, cb ->
            val subQuery = query.subquery(Int::class.java)
            val subRoot = subQuery.from(DbSubmission::class.java)
            subQuery
                .select(cb.max(subRoot.get(SUB_VERSION)))
                .where(cb.equal(root.get<String>(SUB_ACC_NO), subRoot.get<String>(SUB_ACC_NO)))
            cb.equal(root.get<Int>(SUB_VERSION), subQuery)
        }

    private fun withAccession(accNo: String): Specification<DbSubmission> =
        Specification { root, _, cb -> cb.equal(root.get<Int>(SUB_ACC_NO), accNo) }

    private fun withType(type: String): Specification<DbSubmission> =
        Specification { root, _, cb -> cb.equal(root.get<DbSection>(SUB_ROOT_SECTION).get<String>(SECTION_TYPE), type) }

    private fun withTitleLike(title: String): Specification<DbSubmission> =
        Specification { root, _, cb -> cb.like(cb.lower(root.get(SUB_TITLE)), "%${title.toLowerCase()}%") }

    private fun withUser(userId: Long): Specification<DbSubmission> =
        Specification { root, _, cb -> cb.equal(root.get<DbUser>(SUB_OWNER).get<Long>(SUB_OWNER_ID), userId) }

    private fun withFrom(from: OffsetDateTime): Specification<DbSubmission> =
        Specification { root, _, cb -> cb.greaterThan(root.get(SUB_RELEASE_TIME), from.toEpochSecond()) }

    private fun withTo(to: OffsetDateTime): Specification<DbSubmission> =
        Specification { root, _, cb -> cb.lessThan(root.get(SUB_RELEASE_TIME), to.toEpochSecond()) }
}

private fun <T> where(spec: Specification<T>): Specification<T> {
    return Specification.where(spec)!!
}

private infix fun <T> Specification<T>.and(other: Specification<T>): Specification<T> {
    return this.and(other)!!
}
