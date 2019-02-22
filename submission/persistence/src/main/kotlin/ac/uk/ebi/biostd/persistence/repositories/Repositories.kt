package ac.uk.ebi.biostd.persistence.repositories

import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.FULL_DATA_GRAPH
import ac.uk.ebi.biostd.persistence.model.SecurityToken
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.model.Submission
import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.model.UserGroup
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface SubmissionDataRepository : JpaRepository<Submission, Long> {

    @EntityGraph(value = FULL_DATA_GRAPH, type = LOAD)
    fun getByAccNoAndVersionGreaterThan(id: String, long: Int = 0): Submission

    fun findByAccNoAndVersionGreaterThan(id: String, long: Int = 0): Submission?

    fun existsByAccNo(accNo: String): Boolean
}

interface TagsDataRepository : JpaRepository<AccessTag, Long> {

    fun findByName(name: String): AccessTag
}

interface SequenceDataRepository : JpaRepository<Sequence, Long> {

    fun getByPrefixAndSuffix(prefix: String, suffix: String): Sequence
}

interface UserDataRepository : JpaRepository<User, Long> {

    fun findByLoginOrEmail(login: String, email: String): Optional<User>
    fun getByEmail(userEmail: String): User
    fun existsByEmail(email: String): Boolean
}

interface TokenDataRepository : JpaRepository<SecurityToken, String>

interface UserGroupDataRepository : JpaRepository<UserGroup, Long> {

    fun findByNameAndUsersId(groupName: String, userId: Long): Optional<UserGroup>
    fun getByName(groupName: String): UserGroup
}
