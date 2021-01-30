package ac.uk.ebi.biostd.data.service

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.model.DbUserData
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import org.springframework.data.domain.PageRequest

class UserDataService(
    private val dataRepository: UserDataDataRepository,
    private val userDataRepository: UserDataRepository
) {

    fun getUserData(userEmail: String, key: String): DbUserData? = dataRepository.findByUserEmailAndKey(userEmail, key)

    fun saveUserData(userEmail: String, key: String, content: String): DbUserData {
        val user = userDataRepository.getByEmail(userEmail)
        return dataRepository.save(DbUserData(user.id, key, content))
    }

    fun delete(userEmail: String, accNo: String) = dataRepository.deleteByUserEmailAndKey(userEmail, accNo)

    fun findAll(userEmail: String, filter: PaginationFilter = PaginationFilter()): List<DbUserData> =
        dataRepository.findByUserEmail(userEmail, PageRequest.of(filter.pageNumber, filter.limit))
}
