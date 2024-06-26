package ac.uk.ebi.biostd.persistence.model

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Table(name = "UserData")
@Entity
@IdClass(UserDataId::class)
@Suppress("SerialVersionUIDInSerializableClass")
class DbUserData(
    @Id
    val userId: Long,
    @Id
    val key: String,
    var data: String,
) {
    @ManyToOne
    @JoinColumn(name = "userId", updatable = false, insertable = false)
    lateinit var user: DbUser
}

@Suppress("SerialVersionUIDInSerializableClass")
class UserDataId : Serializable {
    var userId: Long = 0

    @Column(name = "dataKey")
    lateinit var key: String

    constructor()

    constructor(userId: Long, key: String) {
        this.userId = userId
        this.key = key
    }
}
