package au.com.shiftyjelly.pocketcasts.podcasts.view.components.ratings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import au.com.shiftyjelly.pocketcasts.compose.AppThemeWithBackground
import au.com.shiftyjelly.pocketcasts.compose.components.TextP40
import au.com.shiftyjelly.pocketcasts.compose.preview.ThemePreviewParameterProvider
import au.com.shiftyjelly.pocketcasts.compose.theme
import au.com.shiftyjelly.pocketcasts.models.entity.PodcastRatings
import au.com.shiftyjelly.pocketcasts.podcasts.viewmodel.PodcastRatingsViewModel
import au.com.shiftyjelly.pocketcasts.podcasts.viewmodel.PodcastRatingsViewModel.RatingState
import au.com.shiftyjelly.pocketcasts.podcasts.viewmodel.PodcastRatingsViewModel.Star
import au.com.shiftyjelly.pocketcasts.ui.theme.Theme
import au.com.shiftyjelly.pocketcasts.utils.extensions.abbreviated
import java.util.UUID

@Composable
fun StarRatingView(
    fragmentManager: FragmentManager,
    viewModel: PodcastRatingsViewModel,
) {
    val state by viewModel.stateFlow.collectAsState()

    when (state) {
        is RatingState.Loaded -> {
            val loadedState = state as RatingState.Loaded
            Content(
                state = loadedState,
                onClick = {
                    viewModel.onRatingStarsTapped(
                        podcastUuid = loadedState.podcastUuid,
                        fragmentManager = fragmentManager,
                    )
                },
            )
        }
        is RatingState.Loading,
        is RatingState.Error,
        -> Unit // Do Nothing
    }
}

@Composable
private fun Content(
    state: RatingState.Loaded,
    onClick: () -> Unit
) {

    if (state.noRatings) return

    Row(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Stars(
            stars = state.stars,
            color = MaterialTheme.theme.colors.filter03
        )

        state.total?.let {
            TextP40(
                text = it.abbreviated,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
private fun Stars(
    stars: List<Star>,
    color: Color,
) {
    Row(horizontalArrangement = Arrangement.Start) {
        stars.forEach { star ->
            Icon(
                imageVector = star.icon,
                contentDescription = null,
                tint = color
            )
        }
    }
}

@Preview
@Composable
private fun PodcastRatingsPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) themeType: Theme.ThemeType,
) {
    AppThemeWithBackground(themeType) {
        Content(
            state = RatingState.Loaded(
                PodcastRatings(
                    podcastUuid = UUID.randomUUID().toString(),
                    average = 3.5,
                    total = 1200
                )
            ),
            onClick = {}
        )
    }
}
