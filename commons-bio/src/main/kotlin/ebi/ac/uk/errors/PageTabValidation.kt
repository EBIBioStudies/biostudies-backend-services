package ebi.ac.uk.errors

class ValidationTree(var status: String, var log: ValidationNode)

class ValidationNode(var level: String, var message: String, var subnodes: MutableList<ValidationNode> = mutableListOf())
