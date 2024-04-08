package ac.uk.ebi.biostd.persistence.model

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

const val DEFAULT_SUFFIX = "000null"

@Entity
@Table(name = "IdGen")
class DbSequence(
    @Column
    val prefix: String,
) {
    @Id
    @GeneratedValue
    var id: Long = 0L

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "counter_id")
    var counter: DbCounter = DbCounter("$prefix$DEFAULT_SUFFIX")
}
