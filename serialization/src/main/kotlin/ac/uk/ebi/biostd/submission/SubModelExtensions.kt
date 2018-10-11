package ac.uk.ebi.biostd.submission

fun List<SimpleAttribute>.names() = this.map { it.name }
fun List<SimpleAttribute>.values() = this.map { it.value }
