package ac.uk.ebi.biostd.persistence.repositories

import ac.uk.ebi.biostd.persistence.model.AccessPermission
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.model.FULL_DATA_GRAPH
import ac.uk.ebi.biostd.persistence.model.SecurityToken
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.model.Submission
import ac.uk.ebi.biostd.persistence.model.Tag
import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.model.UserGroup
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import java.util.Optional
import javax.persistence.LockModeType

interface SubmissionDataRepository : JpaRepository<Submission, Long>, JpaSpecificationExecutor<Submission> {
    @EntityGraph(value = FULL_DATA_GRAPH, type = LOAD)
    fun getByAccNoAndVersionGreaterThan(id: String, long: Int = 0): Submission

    fun findByAccNoAndVersionGreaterThan(id: String, long: Int = 0): Submission?

    @Query("Select max(s.version) from Submission s where s.accNo=?1")
    fun getLastVersion(accNo: String): Int?

    @Query("Update Submission s set s.version = -s.version  where s.accNo=?1 and s.version > 0")
    @Modifying
    fun expireActiveVersions(accNo: String)

    @EntityGraph(value = FULL_DATA_GRAPH, type = LOAD)
    fun getFirstByAccNoOrderByVersionDesc(accNo: String): Submission

    fun existsByAccNo(accNo: String): Boolean

    fun findDistinctByRootSectionTypeAndAccessTagsInAndVersionGreaterThan(
        type: String, accessTags: List<AccessTag>, version: Int): List<Submission>

    @Query("Select sub From Submission sub, Section se Where " +
        "sub.version > 0 and sub.rootSection = se and se.type = :type and sub.accNo in (:accessTags)")
    fun findByTypeAndAccNo(type: String, accessTags: List<String>): List<Submission>

}

interface TagsDataRepository : JpaRepository<AccessTag, Long> {
    fun findByName(name: String): AccessTag
}

interface TagsRefRepository : JpaRepository<Tag, Long> {
    fun findByClassifierAndName(classifier: String, name: String): Tag
}

interface SequenceDataRepository : JpaRepository<Sequence, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun getByPrefixAndSuffix(prefix: String, suffix: String): Sequence
}

interface UserDataRepository : JpaRepository<User, Long> {
    fun findByLoginOrEmailAndActive(login: String, email: String, active: Boolean): Optional<User>
    fun getByEmail(userEmail: String): User
    fun existsByEmail(email: String): Boolean
    fun findByActivationKeyAndActive(key: String, active: Boolean): Optional<User>
    fun findByEmailAndActive(email: String, active: Boolean): Optional<User>
}

interface TokenDataRepository : JpaRepository<SecurityToken, String>

interface UserGroupDataRepository : JpaRepository<UserGroup, Long> {
    fun getByName(groupName: String): UserGroup
}

interface AccessPermissionRepository : JpaRepository<AccessPermission, Long> {
    fun existsByAccessTagInAndAccessType(accessTags: List<String>, accessType: AccessType): Boolean
    fun findByUserIdAndAccessType(userId: Long, accessType: AccessType): List<AccessPermission>
}
