package com.example.rooming.domain.model

data class Room(
    val id: String,
    val name: String,
    val capacity: Int,
    val building: String,
    val equipment: List<String>,
    val description: String,
    val availableTimeSlots: List<TimeSlot>,
    val bookedTimeSlots: List<TimeSlot> = emptyList(),
)
