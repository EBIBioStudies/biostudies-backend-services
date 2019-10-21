package ac.uk.ebi.biostd.persistence.model

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "IdGen")
class Sequence(
    @Column
    var prefix: String,

    @Column
    var suffix: String
) {
    @Id
    @GeneratedValue
    var id: Long = 0L

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "counter_id")
    lateinit var counter: Counter

    constructor(prefix: String) : this (prefix, "") {
        counter = Counter(prefix)
    }
}
