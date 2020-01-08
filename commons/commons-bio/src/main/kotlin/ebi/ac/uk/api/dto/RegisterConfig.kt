package ebi.ac.uk.api.dto

sealed class RegisterConfig

object NonRegistration : RegisterConfig()
class UserRegistration(val name: String, val email: String) : RegisterConfig()
