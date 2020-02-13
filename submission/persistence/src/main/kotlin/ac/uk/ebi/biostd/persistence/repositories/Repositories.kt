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
import ac.uk.ebi.biostd.persistence.model.UserData
import ac.uk.ebi.biostd.persistence.model.UserDataId
import ac.uk.ebi.biostd.persistence.model.UserGroup
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import javax.persistence.LockModeType
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph as GraphSpecification

interface SubmissionDataRepository :
    EntityGraphJpaRepository<Submission, Long>, EntityGraphJpaSpecificationExecutor<Submission> {
    @EntityGraph(value = FULL_DATA_GRAPH, type = LOAD)
    fun getByAccNoAndVersionGreaterThan(id: String, version: Int = 0): Submission

    fun findByAccNoAndVersionGreaterThan(id: String, version: Int = 0): Submission?

    @Query("Select max(s.version) from Submission s where s.accNo=?1")
    fun getLastVersion(accNo: String): Int?

    @Query("Update Submission s set s.version = -s.version  where s.accNo=?1 and s.version > 0")
    @Modifying
    fun expireActiveVersions(accNo: String)

    @EntityGraph(value = FULL_DATA_GRAPH, type = LOAD)
    fun getFirstByAccNoOrderByVersionDesc(accNo: String): Submission

    fun existsByAccNo(accNo: String): Boolean

    fun findByRootSectionTypeAndAccNoInAndVersionGreaterThan(
        type: String,
        tags: List<String>,
        graph: GraphSpecification,
        version: Int = 0
    ): List<Submission>
}

interface AccessTagDataRepo : JpaRepository<AccessTag, Long> {
    fun findByName(name: String): AccessTag
    fun existsByName(name: String): Boolean
}

interface TagDataRepository : JpaRepository<Tag, Long> {
    fun findByClassifierAndName(classifier: String, name: String): Tag
}

interface SequenceDataRepository : JpaRepository<Sequence, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun getByPrefix(prefix: String): Sequence

    fun existsByPrefix(prefix: String): Boolean
}

interface UserDataRepository : JpaRepository<User, Long> {
    fun findByLoginOrEmailAndActive(login: String, email: String, active: Boolean): Optional<User>
    fun getByEmail(userEmail: String): User
    fun existsByEmail(email: String): Boolean
    fun findByActivationKeyAndActive(key: String, active: Boolean): Optional<User>
    fun findByEmailAndActive(email: String, active: Boolean): Optional<User>
    fun findByEmail(email: String): Optional<User>
}

interface TokenDataRepository : JpaRepository<SecurityToken, String>

interface UserGroupDataRepository : JpaRepository<UserGroup, Long> {
    fun getByName(groupName: String): UserGroup
}

interface AccessPermissionRepository : JpaRepository<AccessPermission, Long> {
    fun existsByAccessTagNameInAndAccessType(accessTags: List<String>, accessType: AccessType): Boolean
    fun existsByUserEmailAndAccessTypeAndAccessTagName(user: String, type: AccessType, accessTag: String): Boolean
}

interface UserDataDataRepository : JpaRepository<UserData, UserDataId> {
    fun findByUserIdAndKeyIgnoreCaseContaining(userId: Long, dataKey: String): List<UserData>

    fun findByUserId(userId: Long, pageRequest: Pageable): List<UserData>
}
