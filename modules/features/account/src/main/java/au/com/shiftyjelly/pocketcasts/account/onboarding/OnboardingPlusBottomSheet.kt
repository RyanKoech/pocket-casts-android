package au.com.shiftyjelly.pocketcasts.account.onboarding

import android.content.res.Resources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import au.com.shiftyjelly.pocketcasts.account.onboarding.OnboardingPlusFeatures.PlusOutlinedRowButton
import au.com.shiftyjelly.pocketcasts.account.onboarding.OnboardingPlusFeatures.PlusRowButton
import au.com.shiftyjelly.pocketcasts.account.onboarding.OnboardingPlusFeatures.UnselectedPlusOutlinedRowButton
import au.com.shiftyjelly.pocketcasts.account.onboarding.OnboardingPlusFeatures.plusGradientBrush
import au.com.shiftyjelly.pocketcasts.account.viewmodel.OnboardingPlusBottomSheetState
import au.com.shiftyjelly.pocketcasts.account.viewmodel.OnboardingPlusBottomSheetViewModel
import au.com.shiftyjelly.pocketcasts.compose.components.Clickable
import au.com.shiftyjelly.pocketcasts.compose.components.ClickableTextHelper
import au.com.shiftyjelly.pocketcasts.compose.components.TextH20
import au.com.shiftyjelly.pocketcasts.compose.components.TextP60
import au.com.shiftyjelly.pocketcasts.models.type.TrialSubscriptionPricingPhase
import au.com.shiftyjelly.pocketcasts.preferences.Settings
import java.util.Locale
import au.com.shiftyjelly.pocketcasts.localization.R as LR

@Composable
fun OnboardingPlusBottomSheet() {
    val viewModel = hiltViewModel<OnboardingPlusBottomSheetViewModel>()
    val state = viewModel.state.collectAsState().value
    val subscriptions = (state as? OnboardingPlusBottomSheetState.Loaded)?.subscriptions
        ?: emptyList()

    val resources = LocalContext.current.resources

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFF282829))
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 40.dp)
    ) {
        TextH20(
            text = stringResource(LR.string.onboarding_plus_become_a_plus_member),
            textAlign = TextAlign.Center,
            color = Color.White
        )

        if (state is OnboardingPlusBottomSheetState.Loaded) {
            Spacer(Modifier.height(16.dp))
            AnimatedVisibility(
                visible = state.showTrialInfo,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = animationSpec,
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = animationSpec,
                ),
            ) {
                Box(
                    Modifier
                        .padding(bottom = 16.dp)
                        .background(
                            brush = plusGradientBrush,
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    state
                        .mostRecentlySelectedTrialPhase
                        ?.numPeriodFreeTrial(resources)
                        ?.uppercase(Locale.getDefault())
                        ?.let { text ->
                            TextP60(
                                text = text,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 4.dp
                                ),
                            )
                        }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                subscriptions.forEach { subscription ->
                    if (subscription == state.selectedSubscription) {
                        PlusOutlinedRowButton(
                            text = subscription.recurringPricingPhase.pricePerPeriod(resources),
                            selectedCheckMark = true,
                            onClick = { viewModel.updateSelectedSubscription(subscription) }
                        )
                    } else {
                        UnselectedPlusOutlinedRowButton(
                            text = subscription.recurringPricingPhase.pricePerPeriod(
                                resources
                            ),
                            onClick = { viewModel.updateSelectedSubscription(subscription) }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = state.showTrialInfo,
                enter = expandVertically(animationSpec),
                exit = shrinkVertically(animationSpec),
            ) {
                state.mostRecentlySelectedTrialPhase?.let { trialPhase ->
                    val text = stringResource(
                        LR.string.onboarding_plus_recurring_after_free_trial,
                        recurringAfterString(trialPhase, resources)
                    )
                    TextP60(
                        text = text,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }

        Divider(
            thickness = 1.dp,
            color = Color(0xFFE4E4E4),
            modifier = Modifier
                .padding(vertical = 16.dp)
                .alpha(0.24f)
        )

        PlusRowButton(
            text = stringResource(LR.string.onboarding_plus_start_free_trial_and_subscribe),
            onClick = {},
        )

        Spacer(Modifier.height(16.dp))

        val privacyPolicyText = stringResource(LR.string.onboarding_plus_privacy_policy)
        val termsAndConditionsText = stringResource(LR.string.onboarding_plus_terms_and_conditions)
        val text = stringResource(
            LR.string.onboarding_plus_continuing_agrees_to,
            privacyPolicyText,
            termsAndConditionsText
        )
        val uriHandler = LocalUriHandler.current
        ClickableTextHelper(
            text = text,
            color = Color.White,
            textAlign = TextAlign.Center,
            clickables = listOf(
                Clickable(
                    text = privacyPolicyText,
                    onClick = {
                        uriHandler.openUri(Settings.INFO_PRIVACY_URL)
                    }
                ),
                Clickable(
                    text = termsAndConditionsText,
                    onClick = {
                        uriHandler.openUri(Settings.INFO_TOS_URL)
                    }
                ),
            )
        )
    }
}

private val animationSpec = tween<IntSize>(
    durationMillis = 600,
    easing = EaseInOut
)

private fun recurringAfterString(
    trialSubscriptionPricingPhase: TrialSubscriptionPricingPhase,
    res: Resources
) = "${trialSubscriptionPricingPhase.numPeriodFreeTrial(res)} (${trialSubscriptionPricingPhase.trialEnd()})"
