package it.mealmap.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.mealmap.R
import it.mealmap.ui.theme.HeaderSubtitle

/** Navy header with the layered waves of the design, a subtitle and a title. */
@Composable
fun WaveHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(R.drawable.header_waves),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize(),
        )
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .statusBarsPadding()
                .heightIn(min = height)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(subtitle, style = MaterialTheme.typography.titleSmall, color = HeaderSubtitle)
                Text(
                    title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .semantics { heading() },
                )
            }
            actions()
        }
    }
}
