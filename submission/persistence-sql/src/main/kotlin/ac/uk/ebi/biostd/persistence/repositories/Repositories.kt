package ac.uk.ebi.biostd.persistence.repositories

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.DbSecurityToken
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.model.DbSubmissionRT
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.DbUserGroup
import ac.uk.ebi.biostd.persistence.model.USER_DATA_GRAPH
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import javax.persistence.LockModeType

interface AccessTagDataRepo : JpaRepository<DbAccessTag, Long> {
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
    fun findByLoginOrEmailAndActive(
        login: String,
        email: String,
        active: Boolean,
    ): DbUser?

    fun getByEmail(userEmail: String): DbUser

    fun existsByEmail(email: String): Boolean

    fun existsByEmailAndActive(
        email: String,
        active: Boolean,
    ): Boolean

    fun findByActivationKeyAndActive(
        key: String,
        active: Boolean,
    ): DbUser?

    fun findByActivationKey(key: String): DbUser?

    fun findByEmailAndActive(
        email: String,
        active: Boolean,
    ): DbUser?

    @EntityGraph(value = USER_DATA_GRAPH, type = LOAD)
    fun findByEmail(email: String): DbUser?

    @EntityGraph(value = USER_DATA_GRAPH, type = LOAD)
    fun readByEmail(userEmail: String): DbUser
}

interface TokenDataRepository : JpaRepository<DbSecurityToken, String>

interface UserGroupDataRepository : JpaRepository<DbUserGroup, Long> {
    fun findByName(groupName: String): DbUserGroup?
}

interface AccessPermissionRepository : JpaRepository<DbAccessPermission, Long> {
    fun findAllByUserEmail(email: String): List<DbAccessPermission>

    fun findAllByUserEmailAndAccessType(
        email: String,
        accessType: AccessType,
    ): List<DbAccessPermission>

    fun existsByUserEmailAndAccessTypeAndAccessTagName(
        user: String,
        type: AccessType,
        accessTag: String,
    ): Boolean

    fun deleteByUserEmailAndAccessTypeAndAccessTagName(
        user: String,
        type: AccessType,
        accessTag: String,
    )
}

interface SubmissionRtRepository : JpaRepository<DbSubmissionRT, Long> {
    fun findByAccNo(accNo: String): DbSubmissionRT?
}
