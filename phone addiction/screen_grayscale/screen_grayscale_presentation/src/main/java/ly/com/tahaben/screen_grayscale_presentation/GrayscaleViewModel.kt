package ly.com.tahaben.screen_grayscale_presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ly.com.tahaben.screen_grayscale_domain.use_cases.GrayscaleUseCases
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GrayscaleViewModel @Inject constructor(
    private val grayscaleUseCases: GrayscaleUseCases
) : ViewModel() {

    var state by mutableStateOf(GrayscaleState())
        private set

    init {
        checkServiceStats()
        checkSecureSettingsPermissionStats()
        checkForRootAccess()
        checkAccessibilityPermissionStats()
    }

    fun checkServiceStats() {
        state = state.copy(
            isServiceEnabled = grayscaleUseCases.isGrayscaleEnabled() && grayscaleUseCases.isAccessibilityPermissionGranted(),
            isSecureSettingsPermissionGranted = grayscaleUseCases.isSecureSettingsPermissionGranted(),
            isAccessibilityPermissionGranted = grayscaleUseCases.isAccessibilityPermissionGranted()
        )
    }

    private fun checkSecureSettingsPermissionStats() {
        state = state.copy(
            isSecureSettingsPermissionGranted = grayscaleUseCases.isSecureSettingsPermissionGranted()
        )
        Timber.d("secure permission: ${state.isSecureSettingsPermissionGranted}")
    }

    fun setServiceStats(isEnabled: Boolean) {
        grayscaleUseCases.setGrayscaleState(isEnabled)
        if (!state.isAccessibilityPermissionGranted) {
            askForAccessibilityPermission()
        }
        state = state.copy(
            isServiceEnabled = isEnabled
        )
    }

    fun askForAccessibilityPermission() {
        grayscaleUseCases.askForAccessibilityPermission()
    }

    private fun checkAccessibilityPermissionStats() {
        state = state.copy(
            isAccessibilityPermissionGranted = grayscaleUseCases.isAccessibilityPermissionGranted()
        )
        Timber.d("state = ${state.isAccessibilityPermissionGranted}")
    }

    fun askForSecureSettingsPermissionWithRoot() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            if (grayscaleUseCases.askForSecureSettingsPermission()) {
                state = state.copy(isLoading = false)
                checkSecureSettingsPermissionStats()
            }

        }
    }

    fun checkForRootAccess() {
        state = state.copy(
            isDeviceRooted = grayscaleUseCases.isDeviceRooted()
        )
    }
}