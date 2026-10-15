package com.zenith26.fest.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.network.EventDto
import com.zenith26.fest.ui.theme.*

@Composable
fun EventCard(
    event: EventDto,
    onViewDetails: (EventDto) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFull = event.availableSeats <= 0

    val (categoryBg, categoryText) = when (event.category.lowercase()) {
        "technical" -> Pair(Cyan950, Cyan400)
        "cultural" -> Pair(Fuchsia950, Fuchsia400)
        "sports" -> Pair(Emerald950, Emerald400)
        "gaming" -> Pair(Rose950, Rose400)
        "workshop" -> Pair(Amber950, Amber400)
        else -> Pair(Violet950, Violet400)
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = BorderStroke(1.dp, if (isFull) Slate700.copy(alpha = 0.6f) else Slate700),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top row: Icon + Category Badge + Fee Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = event.icon ?: "📅",
                            fontSize = 24.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(categoryBg)
                                .border(1.dp, categoryText.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = event.category,
                                color = categoryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Fee badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (event.fee > 0) Amber950 else Emerald950)
                            .border(
                                1.dp,
                                if (event.fee > 0) Amber500.copy(alpha = 0.3f) else Emerald500.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (event.fee > 0) "₹${event.fee}" else "Free",
                            color = if (event.fee > 0) Amber400 else Emerald400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = event.title,
                    color = Slate100,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date & Time + Venue
                Text(
                    text = "📅 ${event.eventDate} • ${event.eventTime}",
                    color = Slate300,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "📍 ${event.venue}",
                    color = Slate400,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom row: Seats / Full status + View Details button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isFull) {
                        // FULL Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Rose950)
                                .border(1.dp, Rose500, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "FULL",
                                color = Rose400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    } else {
                        // Available seats indicator
                        Column {
                            Text(
                                text = "${event.availableSeats} / ${event.capacity} seats left",
                                color = if (event.availableSeats < 10) Amber400 else Slate300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            // Linear capacity meter
                            val filledFraction = 1f - (event.availableSeats.toFloat() / event.capacity.toFloat())
                            LinearProgressIndicator(
                                progress = { filledFraction.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (filledFraction > 0.85f) Amber500 else Violet500,
                                trackColor = Slate700,
                            )
                        }
                    }

                    // "View Details" button
                    Button(
                        onClick = { onViewDetails(event) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Violet600,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "View Details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
