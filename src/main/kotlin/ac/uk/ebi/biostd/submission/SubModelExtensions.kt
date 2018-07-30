package ac.uk.ebi.biostd.submission

fun Link.isTableElement(): Boolean {
    return this.tableIndex != NO_TABLE_INDEX
}
