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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rooming.core.common.asBookingCreatedAtLabel
import com.example.rooming.core.common.asDisplayLabel
import com.example.rooming.core.ui.EmptyState
import com.example.rooming.core.ui.InfoChipRow
import com.example.rooming.core.ui.SectionCard
import com.example.rooming.core.ui.R as UiR
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
    val activeCount = uiState.bookings.count { booking -> booking.status == BookingStatus.ACTIVE }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(UiR.string.bookings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        if (uiState.bookings.isEmpty() && !uiState.isLoading) {
            EmptyState(
                title = stringResource(UiR.string.bookings_empty_title),
                description = stringResource(UiR.string.bookings_empty_description),
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
                item {
                    SectionCard(
                        title = stringResource(UiR.string.bookings_summary_title),
                        subtitle = stringResource(UiR.string.bookings_summary_subtitle),
                    ) {
                        InfoChipRow(
                            labels = listOf(
                                stringResource(UiR.string.bookings_total_label, uiState.bookings.size),
                                stringResource(UiR.string.bookings_active_label, activeCount),
                            ),
                        )
                    }
                }
                items(uiState.bookings, key = Booking::id) { booking ->
                    SectionCard(
                        title = booking.roomName,
                        subtitle = booking.timeSlot.asDisplayLabel(),
                    ) {
                        InfoChipRow(
                            labels = listOf(booking.status.asDisplayLabel()),
                        )
                        Text(
                            text = stringResource(
                                UiR.string.booking_status_label,
                                booking.status.asDisplayLabel(),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = stringResource(
                                UiR.string.booking_created_at_label,
                                booking.bookedAt.asBookingCreatedAtLabel(),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (booking.status == BookingStatus.ACTIVE) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                OutlinedButton(onClick = { onCancelBooking(booking.id) }) {
                                    Text(stringResource(UiR.string.cancel_action))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
