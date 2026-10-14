package it.mealmap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.mealmap.R
import it.mealmap.domain.model.Meal
import it.mealmap.ui.theme.DashedOutline

private val TileShape = RoundedCornerShape(12.dp)

/** A lunch or dinner in the week grid: protein, carb and vegetables, or a meal with no category. */
@Composable
fun MealTile(meal: Meal, highlighted: Boolean, modifier: Modifier = Modifier) {
    val outline = if (highlighted) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, TileShape)
    } else {
        Modifier
    }
    Box(modifier.then(outline)) {
        when (meal) {
            is Meal.Food -> FoodTile(meal)
            is Meal.Other -> OtherTile(meal)
        }
        if (meal is Meal.Food &&
            meal.locked
        ) {
            LockBadge(Modifier.align(Alignment.TopEnd).padding(6.dp))
        }
    }
}

@Composable
private fun FoodTile(meal: Meal.Food) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .heightIn(min = 66.dp)
            .background(meal.protein.containerColor, TileShape)
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        val color = meal.protein.contentColor
        // Leave room for the lock badge in the top corner.
        TileLine(
            meal.protein.icon,
            stringResource(meal.protein.label),
            color,
            bold = true,
            modifier = if (meal.locked) Modifier.padding(end = 20.dp) else Modifier,
        )
        TileLine(AppIcons.Carb, stringResource(meal.carb.label), color)
        TileLine(AppIcons.Vegetables, stringResource(R.string.vegetables), color)
    }
}

@Composable
private fun OtherTile(meal: Meal.Other) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .heightIn(min = 66.dp)
            .dashedBorder(DashedOutline, cornerRadius = 12.dp)
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        TileLine(meal.kind.icon, stringResource(meal.kind.label), color, bold = true)
        Text(
            stringResource(R.string.week_uncounted),
            color = color,
            fontSize = 11.sp,
            lineHeight = 14.sp,
        )
    }
}

@Composable
private fun TileLine(
    icon: ImageVector,
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(
            text,
            color = color,
            fontSize = if (bold) 13.sp else 12.sp,
            lineHeight = 16.sp,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LockBadge(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(20.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape),
    ) {
        Icon(
            AppIcons.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(12.dp),
        )
    }
}
