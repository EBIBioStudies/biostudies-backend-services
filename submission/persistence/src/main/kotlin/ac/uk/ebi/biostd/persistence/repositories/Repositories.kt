package ac.uk.ebi.biostd.persistence.repositories

import ac.uk.ebi.biostd.persistence.model.AccessPermission
import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.DbUserData
import ac.uk.ebi.biostd.persistence.model.FULL_DATA_GRAPH
import ac.uk.ebi.biostd.persistence.model.SecurityToken
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.model.SubmissionDb
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
    EntityGraphJpaRepository<SubmissionDb, Long>, EntityGraphJpaSpecificationExecutor<SubmissionDb> {
    @EntityGraph(value = FULL_DATA_GRAPH, type = LOAD)
    fun getByAccNoAndVersionGreaterThan(id: String, version: Int = 0): SubmissionDb?

    fun findByAccNoAndVersionGreaterThan(id: String, version: Int = 0): SubmissionDb?

    @Query("Select max(s.version) from SubmissionDb s where s.accNo=?1")
    fun getLastVersion(accNo: String): Int?

    @Query("Update SubmissionDb s set s.version = -s.version  where s.accNo=?1 and s.version > 0")
    @Modifying
    fun expireActiveVersions(accNo: String)

    @EntityGraph(value = FULL_DATA_GRAPH, type = LOAD)
    fun getFirstByAccNoOrderByVersionDesc(accNo: String): SubmissionDb

    fun existsByAccNo(accNo: String): Boolean

    fun findByRootSectionTypeAndAccNoInAndVersionGreaterThan(
        type: String,
        tags: List<String>,
        graph: GraphSpecification,
        version: Int = 0
    ): List<SubmissionDb>
}

interface AccessTagDataRepo : JpaRepository<DbAccessTag, Long> {
    fun findByName(name: String): DbAccessTag
    fun existsByName(name: String): Boolean
}

interface TagDataRepository : JpaRepository<DbTag, Long> {
    fun findByClassifierAndName(classifier: String, name: String): DbTag
}

interface SequenceDataRepository : JpaRepository<Sequence, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun getByPrefix(prefix: String): Sequence

    fun existsByPrefix(prefix: String): Boolean
}

interface UserDataRepository : JpaRepository<DbUser, Long> {
    fun findByLoginOrEmailAndActive(login: String, email: String, active: Boolean): Optional<DbUser>
    fun getByEmail(userEmail: String): DbUser
    fun existsByEmail(email: String): Boolean
    fun findByActivationKeyAndActive(key: String, active: Boolean): Optional<DbUser>
    fun findByEmailAndActive(email: String, active: Boolean): Optional<DbUser>
    fun findByEmail(email: String): Optional<DbUser>
}

interface TokenDataRepository : JpaRepository<SecurityToken, String>

interface UserGroupDataRepository : JpaRepository<UserGroup, Long> {
    fun getByName(groupName: String): UserGroup
}

interface AccessPermissionRepository : JpaRepository<AccessPermission, Long> {
    fun findByUserIdAndAccessType(userId: Long, accessType: AccessType): List<AccessPermission>
    fun findAllByUserEmailAndAccessType(email: String, accessType: AccessType): List<AccessPermission>
    fun existsByAccessTagNameInAndAccessType(accessTags: List<String>, accessType: AccessType): Boolean
    fun existsByUserEmailAndAccessTypeAndAccessTagName(user: String, type: AccessType, accessTag: String): Boolean
}

interface UserDataDataRepository : JpaRepository<DbUserData, UserDataId> {
    fun deleteByUserIdAndKeyIgnoreCaseContaining(userId: Long, dataKey: String): List<DbUserData>

    fun findByUserId(userId: Long, pageRequest: Pageable): List<DbUserData>
}
