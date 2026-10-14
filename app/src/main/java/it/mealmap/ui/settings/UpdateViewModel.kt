package it.mealmap.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.mealmap.data.AppUpdater
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException

sealed interface UpdateStatus {
    data object Idle : UpdateStatus

    data object Checking : UpdateStatus

    data object UpToDate : UpdateStatus

    data class Downloading(val version: String) : UpdateStatus

    data object Failed : UpdateStatus
}

class UpdateViewModel(private val updater: AppUpdater, private val installedVersionCode: Int) :
    ViewModel() {
    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val status: StateFlow<UpdateStatus> = _status.asStateFlow()

    /** Installs the latest release when it is newer: one tap, then the system installer asks. */
    fun checkForUpdates() {
        viewModelScope.launch {
            _status.value = UpdateStatus.Checking
            _status.value = try {
                val release = updater.latestRelease()
                when {
                    release == null -> UpdateStatus.Failed
                    release.versionCode <= installedVersionCode -> UpdateStatus.UpToDate
                    else -> {
                        _status.value = UpdateStatus.Downloading(release.version)
                        updater.install(release)
                        UpdateStatus.Idle
                    }
                }
            } catch (_: IOException) {
                UpdateStatus.Failed
            } catch (_: SerializationException) {
                UpdateStatus.Failed
            }
        }
    }
}
