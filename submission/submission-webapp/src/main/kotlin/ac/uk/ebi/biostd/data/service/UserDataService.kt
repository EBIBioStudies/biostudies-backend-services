package ac.uk.ebi.biostd.data.service

import ac.uk.ebi.biostd.persistence.model.DbUserData
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository

class UserDataService(
    private val dataRepository: UserDataDataRepository,
) {

    fun getUserData(userEmail: String, key: String): DbUserData? = dataRepository.findByUserEmailAndKey(userEmail, key)
    fun delete(userEmail: String, accNo: String) = dataRepository.deleteByUserEmailAndKey(userEmail, accNo)
}
