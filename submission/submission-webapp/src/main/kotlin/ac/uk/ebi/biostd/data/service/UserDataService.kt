package ac.uk.ebi.biostd.data.service

import ac.uk.ebi.biostd.persistence.filter.PaginationFilter
import ac.uk.ebi.biostd.persistence.model.UserData
import ac.uk.ebi.biostd.persistence.model.UserDataId
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import arrow.core.Option
import arrow.core.toOption
import ebi.ac.uk.base.toOption
import org.springframework.data.domain.PageRequest

class UserDataService(private val dataRepository: UserDataDataRepository) {
    fun getUserData(userId: Long, key: String): UserData? =
        dataRepository.findByUserIdAndDataContaining(userId, key)

    fun saveUserData(userId: Long, key: String, content: String): UserData =
        dataRepository.save(UserData(userId, key, content))

    fun delete(userId: Long, accNo: String) {
        dataRepository.deleteById(UserDataId(userId, accNo))
    }

    fun findAll(userId: Long, filter: PaginationFilter = PaginationFilter()): List<UserData> =
        dataRepository.findByUserId(userId, PageRequest.of(filter.pageNumber, filter.limit))
}
