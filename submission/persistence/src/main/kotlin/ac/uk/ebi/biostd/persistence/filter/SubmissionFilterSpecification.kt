package ac.uk.ebi.biostd.persistence.filter

import ac.uk.ebi.biostd.persistence.model.Submission
import ac.uk.ebi.biostd.persistence.model.User
import ebi.ac.uk.base.applyIfNotBlank
import org.springframework.data.jpa.domain.Specification
import java.time.OffsetDateTime

class SubmissionFilterSpecification(userId: Long, filter: SubmissionFilter) {
    val specification: Specification<Submission>

    init {
        var specs = where(withUser(userId)) and withActiveVersion()
        filter.accNo?.let { specs = specs and (withAccession(it)) }
        filter.keywords?.applyIfNotBlank { specs = specs and (withTitleLike(it)) }
        filter.rTimeTo?.let { specs = specs and (withTo(OffsetDateTime.parse(it))) }
        filter.rTimeFrom?.let { specs = specs and (withFrom(OffsetDateTime.parse(it))) }
        specification = specs
    }

    // TODO: Replace string property names by constants
    private fun withActiveVersion(): Specification<Submission> =
        Specification { root, _, cb -> cb.greaterThan<Int>(root.get<Int>("version"), 0) }

    private fun withAccession(accNo: String): Specification<Submission> =
        Specification { root, _, cb -> cb.equal(root.get<Int>("accNo"), accNo) }

    private fun withTitleLike(title: String): Specification<Submission> =
        Specification { root, _, cb -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%") }

    private fun withUser(userId: Long): Specification<Submission> =
        Specification { root, _, cb -> cb.equal(root.get<User>("owner").get<Long>("id"), userId) }

    private fun withFrom(from: OffsetDateTime): Specification<Submission> =
        Specification { root, _, cb -> cb.greaterThan(root.get("releaseTime"), from.toEpochSecond()) }

    private fun withTo(to: OffsetDateTime): Specification<Submission> =
        Specification { root, _, cb -> cb.lessThan(root.get("releaseTime"), to.toEpochSecond()) }
}

private fun <T> where(spec: Specification<T>): Specification<T> {
    return Specification.where(spec)!!
}

private infix fun <T> Specification<T>.and(other: Specification<T>): Specification<T> {
    return this.and(other)!!
}
