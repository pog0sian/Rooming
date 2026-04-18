package com.example.rooming.data.rooms

import com.example.rooming.domain.model.Room
import com.example.rooming.domain.model.TimeSlot

internal fun sampleRooms(): List<Room> = listOf(
    Room(
        id = "room-101",
        name = "Room 101",
        capacity = 24,
        building = "Building A",
        equipment = listOf("Projector", "Whiteboard", "HDMI"),
        description = "Classic lecture room for small groups and workshops.",
        availableTimeSlots = listOf(
            TimeSlot("2026-04-18", "09:00", "10:30"),
            TimeSlot("2026-04-18", "11:00", "12:30"),
            TimeSlot("2026-04-18", "14:00", "15:30"),
        ),
    ),
    Room(
        id = "room-202",
        name = "Room 202",
        capacity = 40,
        building = "Building B",
        equipment = listOf("Projector", "Video conferencing", "Marker board"),
        description = "Spacious room for hybrid meetings and student presentations.",
        availableTimeSlots = listOf(
            TimeSlot("2026-04-18", "10:00", "11:30"),
            TimeSlot("2026-04-18", "13:00", "14:30"),
            TimeSlot("2026-04-18", "16:00", "17:30"),
        ),
    ),
    Room(
        id = "room-303",
        name = "Room 303",
        capacity = 16,
        building = "Building C",
        equipment = listOf("TV panel", "Power sockets", "Round table"),
        description = "Compact brainstorming room suitable for project teams.",
        availableTimeSlots = listOf(
            TimeSlot("2026-04-18", "08:30", "10:00"),
            TimeSlot("2026-04-18", "12:00", "13:30"),
            TimeSlot("2026-04-18", "15:00", "16:30"),
        ),
    ),
    Room(
        id = "room-404",
        name = "Room 404",
        capacity = 80,
        building = "Main Campus",
        equipment = listOf("Stage", "Sound system", "Streaming camera"),
        description = "Large auditorium for open lectures, defenses and club events.",
        availableTimeSlots = listOf(
            TimeSlot("2026-04-18", "09:30", "11:00"),
            TimeSlot("2026-04-18", "13:30", "15:00"),
            TimeSlot("2026-04-18", "17:00", "18:30"),
        ),
    ),
)
