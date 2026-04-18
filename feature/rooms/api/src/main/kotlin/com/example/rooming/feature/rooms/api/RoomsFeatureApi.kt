package com.example.rooming.feature.rooms.api

object RoomsFeatureApi {
    const val route = "rooms"
    const val roomIdArg = "roomId"
    const val detailsRoutePattern = "$route/{$roomIdArg}"

    fun detailsRoute(roomId: String): String = "$route/$roomId"
}
