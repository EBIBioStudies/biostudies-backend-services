package ac.uk.ebi.biostd.persistence.model

interface Tabular : Sortable {

    var tableIndex: Int
}

interface Sortable {
    var order: Int
}
