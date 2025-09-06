package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.common.properties.StorageMode
import jakarta.persistence.CascadeType.MERGE
import jakarta.persistence.CascadeType.PERSIST
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.Lob
import jakarta.persistence.ManyToMany
import jakarta.persistence.NamedAttributeNode
import jakarta.persistence.NamedEntityGraph
import jakarta.persistence.NamedEntityGraphs
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.OffsetDateTime

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @ManyToMany(fetch = FetchType.EAGER)
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
