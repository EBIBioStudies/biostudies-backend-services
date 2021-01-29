package ac.uk.ebi.biostd.data.service

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.model.DbUserData
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import org.springframework.data.domain.PageRequest

class UserDataService(private val dataRepository: UserDataDataRepository) {

    fun getUserData(userId: Long, key: String): DbUserData? = dataRepository.findByUserIdAndKey(userId, key)

    fun saveUserData(userId: Long, key: String, content: String): DbUserData =
        dataRepository.save(DbUserData(userId, key, content))

    fun delete(userId: Long, accNo: String) = dataRepository.deleteByUserIdAndKey(userId, accNo)

    fun findAll(userId: Long, filter: PaginationFilter = PaginationFilter()): List<DbUserData> =
        dataRepository.findByUserId(userId, PageRequest.of(filter.pageNumber, filter.limit))
}
