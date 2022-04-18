package ac.uk.ebi.biostd.persistence.repositories

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.DbSecurityToken
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.model.DbSubmissionRT
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.DbUserData
import ac.uk.ebi.biostd.persistence.model.DbUserGroup
import ac.uk.ebi.biostd.persistence.model.USER_DATA_GRAPH
import ac.uk.ebi.biostd.persistence.model.UserDataId
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import javax.persistence.LockModeType

interface AccessTagDataRepo : JpaRepository<DbAccessTag, Long> {
    fun getByName(name: String): DbAccessTag
    fun findByName(name: String): DbAccessTag?
    fun existsByName(name: String): Boolean
}

interface TagDataRepository : JpaRepository<DbTag, Long>

interface SequenceDataRepository : JpaRepository<DbSequence, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByPrefix(prefix: String): DbSequence?

    fun existsByPrefix(prefix: String): Boolean
}

@Suppress("TooManyFunctions")
interface UserDataRepository : JpaRepository<DbUser, Long> {
    fun findByLoginOrEmailAndActive(login: String, email: String, active: Boolean): DbUser?
    fun getByEmail(userEmail: String): DbUser
    fun existsByEmail(email: String): Boolean
    fun existsByEmailAndActive(email: String, active: Boolean): Boolean
    fun findByActivationKeyAndActive(key: String, active: Boolean): DbUser?
    fun findByActivationKey(key: String): DbUser?
    fun findByEmailAndActive(email: String, active: Boolean): DbUser?
    fun getByEmailAndActive(email: String, active: Boolean): DbUser
    fun findByEmail(email: String): DbUser?
    fun deleteByEmail(email: String)

    @EntityGraph(value = USER_DATA_GRAPH, type = LOAD)
    fun readByEmail(userEmail: String): DbUser

    @EntityGraph(value = USER_DATA_GRAPH, type = LOAD)
    fun getById(id: Long): DbUser
}

interface TokenDataRepository : JpaRepository<DbSecurityToken, String>

interface UserGroupDataRepository : JpaRepository<DbUserGroup, Long> {
    fun findByName(groupName: String): DbUserGroup?
}

interface AccessPermissionRepository : JpaRepository<DbAccessPermission, Long> {
    fun findAllByUserEmailAndAccessType(email: String, accessType: AccessType): List<DbAccessPermission>
    fun existsByUserEmailAndAccessTypeAndAccessTagName(user: String, type: AccessType, accessTag: String): Boolean
}

interface UserDataDataRepository : JpaRepository<DbUserData, UserDataId> {
    fun findByUserEmailAndKey(userEmail: String, key: String): DbUserData?
    fun findByUserEmail(userEmail: String, pageRequest: Pageable): List<DbUserData>

    @Modifying
    fun deleteByUserEmailAndKey(email: String, key: String)
}

interface SubmissionRtRepository : JpaRepository<DbSubmissionRT, Long> {
    fun findByAccNo(accNo: String): DbSubmissionRT?
}
