package au.com.shiftyjelly.pocketcasts.settings.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import au.com.shiftyjelly.pocketcasts.analytics.AnalyticsEvent
import au.com.shiftyjelly.pocketcasts.analytics.AnalyticsTrackerWrapper
import au.com.shiftyjelly.pocketcasts.preferences.Settings
import au.com.shiftyjelly.pocketcasts.repositories.refresh.RefreshPodcastsTask
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import au.com.shiftyjelly.pocketcasts.localization.R as LR

@HiltViewModel
class AdvancedSettingsViewModel
@Inject constructor(
    private val settings: Settings,
    private val analyticsTracker: AnalyticsTrackerWrapper,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val mutableState = MutableStateFlow(initState())
    val state: StateFlow<State> = mutableState

    private val backgroundRefreshSummary: Int
        get() = if (settings.syncOnMeteredNetwork()) {
            LR.string.settings_advanced_sync_on_metered_on
        } else {
            LR.string.settings_advanced_sync_on_metered_off
        }

    private fun initState() = State(
        refreshPodcastsAutomatically = settings.refreshPodcastsAutomatically(),
        backgroundSyncOnMeteredState = State.BackgroundSyncOnMeteredState(
            summary = backgroundRefreshSummary,
            isChecked = settings.syncOnMeteredNetwork(),
            onCheckedChange = {
                onSyncOnMeteredCheckedChange(it)
                analyticsTracker.track(
                    AnalyticsEvent.SETTINGS_ADVANCED_SYNC_ON_METERED,
                    mapOf("enabled" to it)
                )
            }
        )
    )

    private fun onSyncOnMeteredCheckedChange(isChecked: Boolean) {
        settings.setSyncOnMeteredNetwork(isChecked)
        updateSyncOnMeteredState()

        // Update worker to take sync setting into account
        RefreshPodcastsTask.scheduleOrCancel(context, settings)
    }

    private fun updateSyncOnMeteredState() {
        mutableState.value = mutableState.value.copy(
            backgroundSyncOnMeteredState = mutableState.value.backgroundSyncOnMeteredState.copy(
                isChecked = settings.syncOnMeteredNetwork(),
                summary = backgroundRefreshSummary
            )
        )
    }

    fun onShown() {
        analyticsTracker.track(AnalyticsEvent.SETTINGS_ADVANCED_SHOWN)
    }

    data class State(
        val refreshPodcastsAutomatically: Boolean,
        val backgroundSyncOnMeteredState: BackgroundSyncOnMeteredState
    ) {

        data class BackgroundSyncOnMeteredState(
            @StringRes val summary: Int,
            val isChecked: Boolean = true,
            val onCheckedChange: (Boolean) -> Unit,
        )
    }
}
