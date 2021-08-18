package ac.uk.ebi.biostd.persistence.repositories

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.DbSection
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.DbSubmissionRT
import ac.uk.ebi.biostd.persistence.model.DbSubmissionRequest
import ac.uk.ebi.biostd.persistence.model.DbSubmissionStat
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.DbUserData
import ac.uk.ebi.biostd.persistence.model.SECTION_SIMPLE_GRAPH
import ac.uk.ebi.biostd.persistence.model.SUBMISSION_FULL_GRAPH
import ac.uk.ebi.biostd.persistence.model.SecurityToken
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.model.USER_DATA_GRAPH
import ac.uk.ebi.biostd.persistence.model.UserDataId
import ac.uk.ebi.biostd.persistence.model.UserGroup
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor
import ebi.ac.uk.model.constants.ProcessingStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.Optional
import javax.persistence.LockModeType
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph as GraphSpecification

@Suppress("TooManyFunctions")
@Transactional
interface SubmissionDataRepository :
    EntityGraphJpaRepository<DbSubmission, Long>, EntityGraphJpaSpecificationExecutor<DbSubmission> {

    @Modifying
    @Query("update DbSubmission s set s.version = -s.version, s.modificationTime = :now where accNo in :accNumbers")
    fun deleteSubmissions(@Param("accNumbers") accNumbers: List<String>, @Param("now") now: OffsetDateTime)

    @Query("select s from DbSubmission s inner join s.owner where s.accNo = :accNo order by s.id desc")
    fun getBasicAllVersions(@Param("accNo") accNo: String, pageable: Pageable): List<DbSubmission>

    @JvmDefault
    fun getLastVersion(accNo: String): DbSubmission? = getBasicAllVersions(accNo, PageRequest.of(0, 1)).firstOrNull()

    @Query(
        """
        select s
        from DbSubmission s inner join s.owner inner join s.attributes
        where s.accNo = :accNo and s.version > 0
    """
    )
    fun findBasicWithAttributes(@Param("accNo") accNo: String): DbSubmission?

    @EntityGraph(value = SUBMISSION_FULL_GRAPH, type = LOAD)
    fun getByAccNoAndVersionGreaterThan(accNo: String, version: Int = 0): DbSubmission?

    @EntityGraph(value = SUBMISSION_FULL_GRAPH, type = LOAD)
    fun getByAccNoAndVersion(accNo: String, version: Int): DbSubmission?

    @Query(
        """
        Update DbSubmission s Set s.version = -s.version
        Where s.accNo=?1 And s.version > 0 And status = 'PROCESSED'
    """
    )
    @Modifying
    fun expireActiveProcessedVersions(accNo: String)

    @Query("Update DbSubmission s set s.status = ?1 Where s.accNo = ?2 and s.version = ?3")
    @Modifying
    fun updateStatus(status: ProcessingStatus, accNo: String, version: Int)

    fun existsByAccNo(accNo: String): Boolean

    @Query("select s from DbSubmission s Where s.accNo = ?1 and s.status = PROCESSED and s.version > 0")
    @Modifying
    fun existsByAccNoAndProcessedAndActive(accNo: String): Boolean

    fun findByRootSectionTypeAndAccNoInAndVersionGreaterThan(
        type: String,
        tags: List<String>,
        graph: GraphSpecification,
        version: Int = 0
    ): List<DbSubmission>
}

interface SubmissionRequestDataRepository : JpaRepository<DbSubmissionRequest, Long> {
    fun getByAccNoAndVersion(accNo: String, version: Int): DbSubmissionRequest
}

interface SectionDataRepository : JpaRepository<DbSection, Long> {
    @EntityGraph(value = SECTION_SIMPLE_GRAPH, type = LOAD)
    fun getById(sectionId: Long): DbSection

    @Query("select s.id from DbSection s where s.submission.id = :id and s.id <> :rootSectionId")
    fun sections(@Param("id") id: Long, @Param("rootSectionId") rootSectionId: Long): List<Long>
}

interface AccessTagDataRepo : JpaRepository<DbAccessTag, Long> {
    fun getByName(name: String): DbAccessTag
    fun findByName(name: String): DbAccessTag?
    fun existsByName(name: String): Boolean
    fun findBySubmissionsAccNo(accNo: String): List<DbAccessTag>
}

interface TagDataRepository : JpaRepository<DbTag, Long> {
    fun findByClassifierAndName(classifier: String, name: String): DbTag
}

interface SequenceDataRepository : JpaRepository<Sequence, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByPrefix(prefix: String): Sequence?

    fun existsByPrefix(prefix: String): Boolean
}

interface UserDataRepository : JpaRepository<DbUser, Long> {
    fun findByLoginOrEmailAndActive(login: String, email: String, active: Boolean): Optional<DbUser>
    fun getByEmail(userEmail: String): DbUser
    fun existsByEmail(email: String): Boolean
    fun existsByEmailAndActive(email: String, active: Boolean): Boolean
    fun findByActivationKeyAndActive(key: String, active: Boolean): Optional<DbUser>
    fun findByEmailAndActive(email: String, active: Boolean): Optional<DbUser>
    fun findByEmail(email: String): Optional<DbUser>

    @EntityGraph(value = USER_DATA_GRAPH, type = LOAD)
    fun readByEmail(userEmail: String): DbUser

    @EntityGraph(value = USER_DATA_GRAPH, type = LOAD)
    fun getById(id: Long): DbUser
}

interface TokenDataRepository : JpaRepository<SecurityToken, String>

interface UserGroupDataRepository : JpaRepository<UserGroup, Long> {
    fun getByName(groupName: String): UserGroup
}

interface AccessPermissionRepository : JpaRepository<DbAccessPermission, Long> {
    fun findAllByUserEmailAndAccessType(email: String, accessType: AccessType): List<DbAccessPermission>
    fun existsByUserEmailAndAccessTypeAndAccessTagName(user: String, type: AccessType, accessTag: String): Boolean
}

interface UserDataDataRepository : JpaRepository<DbUserData, UserDataId> {
    fun findByUserEmailAndKey(userEmail: String, key: String): DbUserData?
    fun findByUserEmail(userEmail: String, pageRequest: Pageable): List<DbUserData>

    @Modifying
    fun deleteByKey(key: String)

    @Modifying
    fun deleteByUserEmailAndKey(email: String, key: String)
}

interface SubmissionStatsDataRepository : PagingAndSortingRepository<DbSubmissionStat, Long> {
    fun findByAccNo(accNo: String): List<DbSubmissionStat>
    fun existsByAccNoAndType(accNo: String, type: SubmissionStatType): Boolean
    fun findAllByType(type: SubmissionStatType, pageable: Pageable): Page<DbSubmissionStat>
    fun findByAccNoAndType(accNo: String, type: SubmissionStatType): DbSubmissionStat?
    fun getByAccNoAndType(accNo: String, type: SubmissionStatType): DbSubmissionStat
}

interface SubmissionRtRepository : JpaRepository<DbSubmissionRT, Long> {
    fun findByAccNo(accNo: String): DbSubmissionRT?
}
