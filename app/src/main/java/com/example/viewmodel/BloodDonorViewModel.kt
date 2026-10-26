package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BloodCompatibilityUtil
import com.example.data.model.BloodRequestEntity
import com.example.data.model.UserEntity
import com.example.data.repository.BloodDonorRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DonorFilterState(
    val selectedBloodGroup: String? = null,
    val searchQuery: String = "",
    val availableOnly: Boolean = false
)

class BloodDonorViewModel(
    private val repository: BloodDonorRepository,
    currentUserIdProvider: () -> Int?
) : ViewModel() {

    private val _filterState = MutableStateFlow(DonorFilterState())
    val filterState: StateFlow<DonorFilterState> = _filterState.asStateFlow()

    private val _currentUserId = MutableStateFlow<Int?>(currentUserIdProvider())
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    private val _videoUrl = MutableStateFlow(BloodDonorRepository.DEFAULT_VIDEO_URL)
    val videoUrl: StateFlow<String> = _videoUrl.asStateFlow()

    private val _emergencyAlertFlow = MutableSharedFlow<BloodRequestEntity>(extraBufferCapacity = 1)
    val emergencyAlertFlow: SharedFlow<BloodRequestEntity> = _emergencyAlertFlow.asSharedFlow()

    private val knownRequestIds = mutableSetOf<Int>()
    private var isFirstRequestsLoad = true

    init {
        refreshFromCloud()
        loadVideoUrl()
        observeRequestsForAlerts()
    }

    private fun observeRequestsForAlerts() {
        viewModelScope.launch {
            repository.allRequests.collect { requests ->
                if (isFirstRequestsLoad) {
                    knownRequestIds.addAll(requests.map { it.id })
                    isFirstRequestsLoad = false
                } else {
                    val userBloodGroup = currentUserProfile.value?.bloodGroup
                    for (req in requests) {
                        if (!knownRequestIds.contains(req.id) && !req.isFulfilled) {
                            knownRequestIds.add(req.id)
                            if (userBloodGroup != null && shouldAlertDonor(userBloodGroup, req.bloodGroup)) {
                                _emergencyAlertFlow.emit(req)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun shouldAlertDonor(donorGroup: String, requestedGroup: String): Boolean {
        if (donorGroup.equals(requestedGroup, ignoreCase = true)) return true
        val canDonateTo = BloodCompatibilityUtil.canDonateTo(donorGroup)
        return canDonateTo.any { it.equals(requestedGroup, ignoreCase = true) }
    }

    private fun loadVideoUrl() {
        viewModelScope.launch {
            _videoUrl.value = repository.getHomeVideoUrl()
        }
    }

    fun refreshFromCloud() {
        viewModelScope.launch {
            repository.syncFromCloud()
        }
    }

    val currentUserProfile: StateFlow<UserEntity?> = combine(
        _currentUserId,
        repository.allDonors
    ) { userId, donors ->
        if (userId != null) donors.find { it.id == userId } else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allDonors: StateFlow<List<UserEntity>> = repository.allDonors.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered donors list
    val filteredDonors: StateFlow<List<UserEntity>> = combine(
        repository.allDonors,
        _filterState
    ) { donors, filter ->
        donors.filter { donor ->
            val matchesBloodGroup = filter.selectedBloodGroup == null ||
                    donor.bloodGroup.equals(filter.selectedBloodGroup, ignoreCase = true)
            val matchesQuery = filter.searchQuery.isBlank() ||
                    donor.location.contains(filter.searchQuery, ignoreCase = true) ||
                    donor.fullName.contains(filter.searchQuery, ignoreCase = true) ||
                    donor.bloodGroup.contains(filter.searchQuery, ignoreCase = true)
            val matchesAvailability = !filter.availableOnly || donor.isAvailable
            matchesBloodGroup && matchesQuery && matchesAvailability
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allBloodRequests: StateFlow<List<BloodRequestEntity>> = repository.allRequests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setCurrentUserId(id: Int?) {
        _currentUserId.value = id
    }

    fun setBloodGroupFilter(bloodGroup: String?) {
        _filterState.value = _filterState.value.copy(
            selectedBloodGroup = if (_filterState.value.selectedBloodGroup == bloodGroup) null else bloodGroup
        )
    }

    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun toggleAvailableOnly(availableOnly: Boolean) {
        _filterState.value = _filterState.value.copy(availableOnly = availableOnly)
    }

    fun clearFilters() {
        _filterState.value = DonorFilterState()
    }

    fun toggleAvailability(isAvailable: Boolean) {
        val userId = _currentUserId.value ?: return
        viewModelScope.launch {
            repository.updateAvailability(userId, isAvailable)
        }
    }

    fun logDonation(date: String) {
        val userId = _currentUserId.value ?: return
        viewModelScope.launch {
            repository.logDonation(userId, date)
        }
    }

    fun updateProfile(phone: String, location: String, age: Int) {
        val currentUser = currentUserProfile.value ?: return
        viewModelScope.launch {
            val updated = currentUser.copy(
                phoneNumber = phone,
                location = location,
                age = age
            )
            repository.updateUser(updated)
        }
    }

    fun createBloodRequest(
        patientName: String,
        bloodGroup: String,
        unitsNeeded: Int,
        hospitalName: String,
        hospitalLocation: String,
        contactPhone: String,
        urgency: String,
        notes: String
    ) {
        viewModelScope.launch {
            val requesterName = currentUserProfile.value?.fullName ?: "Anonymous"
            val newRequest = BloodRequestEntity(
                patientName = patientName,
                bloodGroup = bloodGroup,
                unitsNeeded = unitsNeeded,
                hospitalName = hospitalName,
                hospitalLocation = hospitalLocation,
                contactPhone = contactPhone,
                urgency = urgency,
                notes = notes,
                requesterName = requesterName
            )
            val insertedId = repository.createBloodRequest(newRequest)
            val reqWithId = newRequest.copy(id = insertedId.toInt())
            knownRequestIds.add(reqWithId.id)
            _emergencyAlertFlow.emit(reqWithId)
        }
    }

    fun markRequestFulfilled(requestId: Int) {
        viewModelScope.launch {
            repository.markRequestFulfilled(requestId)
        }
    }
}

class BloodDonorViewModelFactory(
    private val repository: BloodDonorRepository,
    private val currentUserIdProvider: () -> Int?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BloodDonorViewModel::class.java)) {
            return BloodDonorViewModel(repository, currentUserIdProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
