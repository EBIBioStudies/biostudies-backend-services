package ac.uk.ebi.biostd.tsv.serialization

internal fun TsvBuilder.addSeparator() {
    append("\n")
}

internal fun TsvBuilder.addSubAcc(accNo: String) {
    append("$ACC_NO_KEY\t$accNo")
    addSeparator()
}
