package ebi.ac.uk.errors

class ValidationTree(var status: ValidationTreeStatus, var log: ValidationNode)

enum class ValidationTreeStatus {
    FAIL,
}

class ValidationNode(
    var level: ValidationNodeStatus,
    var message: String,
    var subnodes: List<ValidationNode> = emptyList(),
)

enum class ValidationNodeStatus {
    ERROR,
}
