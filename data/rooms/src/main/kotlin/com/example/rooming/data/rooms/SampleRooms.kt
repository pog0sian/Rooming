package com.example.rooming.data.rooms

import com.example.rooming.domain.model.Room
import com.example.rooming.domain.model.TimeSlot

internal fun sampleRooms(): List<Room> = listOf(
    Room(
        id = "room-101",
        name = "Аудитория 101",
        capacity = 24,
        building = "Корпус А",
        equipment = listOf("Проектор", "Маркерная доска", "HDMI"),
        description = "Классическая учебная аудитория для семинаров, практик и мини-лекций.",
        availableTimeSlots = listOf(
            TimeSlot("2026-04-18", "09:00", "10:30"),
            TimeSlot("2026-04-18", "11:00", "12:30"),
            TimeSlot("2026-04-18", "14:00", "15:30"),
        ),
    ),
    Room(
        id = "room-202",
        name = "Аудитория 202",
        capacity = 40,
        building = "Корпус Б",
        equipment = listOf("Проектор", "Видеосвязь", "Маркерная доска"),
        description = "Просторная аудитория для гибридных встреч, докладов и презентаций.",
        availableTimeSlots = listOf(
            TimeSlot("2026-04-18", "10:00", "11:30"),
            TimeSlot("2026-04-18", "13:00", "14:30"),
            TimeSlot("2026-04-18", "16:00", "17:30"),
        ),
    ),
    Room(
        id = "room-303",
        name = "Аудитория 303",
        capacity = 16,
        building = "Корпус В",
        equipment = listOf("ТВ-панель", "Розетки", "Круглый стол"),
        description = "Компактная аудитория для мозговых штурмов и работы проектной команды.",
        availableTimeSlots = listOf(
            TimeSlot("2026-04-18", "08:30", "10:00"),
            TimeSlot("2026-04-18", "12:00", "13:30"),
            TimeSlot("2026-04-18", "15:00", "16:30"),
        ),
    ),
    Room(
        id = "room-404",
        name = "Аудитория 404",
        capacity = 80,
        building = "Главный корпус",
        equipment = listOf("Сцена", "Акустика", "Камера для трансляций"),
        description = "Большая аудитория для открытых лекций, защит и студенческих мероприятий.",
        availableTimeSlots = listOf(
            TimeSlot("2026-04-18", "09:30", "11:00"),
            TimeSlot("2026-04-18", "13:30", "15:00"),
            TimeSlot("2026-04-18", "17:00", "18:30"),
        ),
    ),
)
