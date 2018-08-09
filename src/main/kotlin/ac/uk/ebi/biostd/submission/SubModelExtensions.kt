package ac.uk.ebi.biostd.submission

fun List<Term>.names() = this.map { it.name }
fun List<Term>.values() = this.map { it.value }