package com.dugue.canipark.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.entities.BitmapRequest
import domain.entities.ParkingRequest
import domain.entities.ParkingResponse
import domain.repositories.ParkingSignsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraViewModel(private val repository: ParkingSignsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUIState())
    val uiState: StateFlow<CameraUIState> = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        _uiState.value
    )

    private val _events = MutableSharedFlow<CameraEvent>()
    private val events = _events.asSharedFlow()

    init {
        subscribeToEvents()
    }

    fun onEvent(event: CameraEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            _events.emit(event)
        }
    }

    private fun subscribeToEvents() {
        events.onEach { event ->
            when (event) {
                is CameraEvent.PictureTaken -> pictureTaken(event.parkingRequest)
                is CameraEvent.ResultDismissed -> dismissResult()
                is CameraEvent.PictureError -> _uiState.update {
                    it.copy(cameraState = CameraState.Error(event.message))
                }
                is CameraEvent.PictureTakenBitmap -> pictureTaken(event.bitmapRequest)
            }
        }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    private suspend fun pictureTaken(parkingRequest: ParkingRequest) {
        _uiState.update { it.copy(cameraState = CameraState.Loading) }
        val result = repository.analyzeParkingSigns(parkingRequest)
        if (result.isSuccess) {
            _uiState.update {
                it.copy(
                    cameraState = if (result.getOrThrow().canIPark) {
                        CameraState.ParkingAllowed(buildMessage(result.getOrThrow()))
                    } else {
                        CameraState.ParkingNotAllowed(buildMessage(result.getOrThrow()))
                    }
                )
            }
        } else {
            _uiState.update { it.copy(cameraState = CameraState.Error(result.exceptionOrNull()!!.localizedMessage)) }
        }
    }

    private suspend fun pictureTaken(bitmapRequest: BitmapRequest) {
        _uiState.update { it.copy(cameraState = CameraState.Loading) }
        val result = repository.analyzeParkingSigns(bitmapRequest)
        if (result.isSuccess) {
            _uiState.update {
                it.copy(
                    cameraState = if (result.getOrThrow().canIPark) {
                        CameraState.ParkingAllowed(buildMessage(result.getOrThrow()))
                    } else {
                        CameraState.ParkingNotAllowed(buildMessage(result.getOrThrow()))
                    }
                )
            }
        } else {
            _uiState.update { it.copy(cameraState = CameraState.Error(result.exceptionOrNull()!!.localizedMessage)) }
        }
    }

    private suspend fun dismissResult() {
        _uiState.update { it.copy(cameraState = CameraState.Idle) }
    }

    private fun buildMessage(parkingResponse: ParkingResponse): String {
        val builder = StringBuilder()
        if (parkingResponse.canIPark) {
            builder.append("You can park here")
            if (parkingResponse.howLong != null) {
                builder.append(" for ${parkingResponse.howLong} minutes.")
            }
            if (parkingResponse.cost != null) {
                builder.append("It will cost you ${parkingResponse.cost}.")
            }
        } else {
            builder.append("You cannot park here")
            if (parkingResponse.reasonIfNo != null) {
                builder.append(" because ${parkingResponse.reasonIfNo}.")
            }
        }
        return builder.toString()
    }
}
