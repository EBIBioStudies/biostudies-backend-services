package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.model.AccessPermission
import ac.uk.ebi.biostd.persistence.common.model.AccessType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "AccessPermission")
class DbAccessPermission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Enumerated(EnumType.STRING)
    @Column(name = "access_type")
    override val accessType: AccessType,
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: DbUser,
    @ManyToOne
    @JoinColumn(name = "access_tag_id")
    override val accessTag: DbAccessTag,
) : AccessPermission
