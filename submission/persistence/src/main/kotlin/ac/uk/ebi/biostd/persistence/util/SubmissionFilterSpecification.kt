package ac.uk.ebi.biostd.persistence.util

import ac.uk.ebi.biostd.persistence.model.Submission
import ac.uk.ebi.biostd.persistence.model.User
import org.springframework.data.jpa.domain.Specification

class SubmissionFilterSpecification(userId: Long, filter: SubmissionFilter) {
    var specification: Specification<Submission>

    init {
        specification = Specification.where(
            withUser(userId)).and(withVersionGreaterThan(0))
        filter.accNo?.let { specification = specification.and(withAccession(it)) }
        filter.rTimeFrom?.let { specification = specification.and(withFrom(it)) }
        filter.rTimeTo?.let { specification = specification.and(withTo(it)) }
        filter.keywords?.let {
            if (filter.keywords.isNotBlank())
                specification = specification.and(withTitleLike(it))
        }
    }

    private fun withVersionGreaterThan(version: Int): Specification<Submission> {
        return Specification { root, query, cb ->
            cb.greaterThan<Int>(root.get<Int>("version"), version)
        }
    }

    private fun withAccession(accNo: String): Specification<Submission> {
        return Specification { root, query, cb ->
            cb.equal(root.get<Int>("accNo"), accNo)
        }
    }

    private fun withTitleLike(title: String): Specification<Submission> {
        return Specification { root, query, cb ->
            cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%")
        }
    }

    private fun withUser(userId: Long): Specification<Submission> {
        return Specification { root, query, cb ->
            cb.equal(root.get<User>("owner").get<Long>("id"), userId)
        }
    }

    private fun withFrom(from: Long): Specification<Submission> {
        return Specification { root, query, cb ->
            cb.greaterThan(root.get("releaseTime"), from)
        }
    }

    private fun withTo(to: Long): Specification<Submission> {
        return Specification { root, query, cb ->
            cb.lessThan(root.get("releaseTime"), to)
        }
    }
}
