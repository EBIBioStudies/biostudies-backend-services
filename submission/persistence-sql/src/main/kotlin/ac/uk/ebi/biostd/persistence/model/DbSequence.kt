package ac.uk.ebi.biostd.persistence.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

const val DEFAULT_SUFFIX = "000null"

@Entity
@Table(name = "IdGen")
class DbSequence(
    @Column
    val prefix: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "counter_id")
    var counter: DbCounter = DbCounter("$prefix$DEFAULT_SUFFIX")
}
