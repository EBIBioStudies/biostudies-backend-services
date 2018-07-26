package ac.uk.ebi.biostd.submission

fun Link.isTable(): Boolean {
    return this.tableIndex != NO_TABLE_INDEX
}
