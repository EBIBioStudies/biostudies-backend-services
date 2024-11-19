package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.common.properties.StorageMode
import java.time.OffsetDateTime
import javax.persistence.CascadeType.MERGE
import javax.persistence.CascadeType.PERSIST
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.Lob
import javax.persistence.ManyToMany
import javax.persistence.NamedAttributeNode
import javax.persistence.NamedEntityGraph
import javax.persistence.NamedEntityGraphs
import javax.persistence.OneToMany
import javax.persistence.Table

internal const val USER_DATA_GRAPH = "DbUser.fullData"

typealias Node = NamedAttributeNode

@NamedEntityGraphs(
    value = [
        NamedEntityGraph(
            name = USER_DATA_GRAPH,
            attributeNodes = [
                Node("groups"),
                Node("permissions"),
            ],
        ),
    ],
)
@Entity
@Table(name = "User")
@Suppress("LongParameterList")
class DbUser(
    @Id
    @GeneratedValue
    var id: Long = 0L,
    @Column
    var email: String,
    @Column
    var fullName: String,
    @Column
    var keyTime: Long = OffsetDateTime.now().toInstant().toEpochMilli(),
    @Column
    var secret: String,
    @Lob
    var passwordDigest: ByteArray,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "UserGroup_User",
        joinColumns = [JoinColumn(name = "users_id")],
        inverseJoinColumns = [JoinColumn(name = "groups_id")],
    )
    val groups: MutableSet<DbUserGroup> = mutableSetOf(),
    @Column
    var superuser: Boolean = false,
    @Column
    var notificationsEnabled: Boolean = false,
    @Enumerated(EnumType.STRING)
    @Column(name = "storageMode")
    var storageMode: StorageMode,
    @Column
    var orcid: String? = null,
) {
    @Column
    var active: Boolean = false

    var login: String? = null

    @Column
    var activationKey: String? = null

    @OneToMany(mappedBy = "user", cascade = [PERSIST, MERGE])
    val permissions: Set<DbAccessPermission> = emptySet()
}

fun DbUser.addGroup(userGroup: DbUserGroup): DbUser = also { groups.add(userGroup) }
