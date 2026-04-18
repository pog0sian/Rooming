package com.example.rooming.feature.bookings.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rooming.core.common.asDisplayLabel
import com.example.rooming.core.ui.EmptyState
import com.example.rooming.core.ui.SectionCard
import com.example.rooming.domain.model.Booking
import com.example.rooming.domain.model.BookingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsRoute(
    modifier: Modifier = Modifier,
    viewModel: BookingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BookingsScreen(
        uiState = uiState,
        onCancelBooking = viewModel::onCancelBooking,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    uiState: BookingsUiState,
    onCancelBooking: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("My bookings") }) },
    ) { innerPadding ->
        if (uiState.bookings.isEmpty() && !uiState.isLoading) {
            EmptyState(
                title = "No bookings yet",
                description = "Create a booking from the room details screen.",
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.bookings, key = Booking::id) { booking ->
                    SectionCard(
                        title = booking.roomName,
                        subtitle = booking.timeSlot.asDisplayLabel(),
                    ) {
                        Text(
                            text = "Status: ${booking.status.asDisplayLabel()}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "Created at: ${booking.bookedAt}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (booking.status == BookingStatus.ACTIVE) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                OutlinedButton(onClick = { onCancelBooking(booking.id) }) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
