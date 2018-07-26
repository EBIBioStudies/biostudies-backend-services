package ac.uk.ebi.biostd.submission

fun Link.isTable(): Boolean {
    return this.tableIndex != noTableIndex
}