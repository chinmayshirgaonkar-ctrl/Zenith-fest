package com.zenith26.fest.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ZenithApiService {

    @GET("api/events")
    suspend fun getEvents(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): List<EventDto>

    @GET("api/events/{event_id}")
    suspend fun getEvent(
        @Path("event_id") eventId: Int
    ): EventDto

    @POST("api/events/{event_id}/register")
    suspend fun registerForEvent(
        @Path("event_id") eventId: Int,
        @Body body: RegistrationInDto
    ): Response<RegistrationOutDto>

    @GET("api/registrations")
    suspend fun getRegistrations(
        @Query("event_id") eventId: Int? = null
    ): List<RegistrationDetailDto>

    @GET("api/stats")
    suspend fun getStats(): StatsDto

    // Stretch goal endpoints
    @POST("api/events")
    suspend fun createEvent(
        @Body body: EventCreateDto
    ): Response<EventDto>

    @DELETE("api/registrations/{registration_id}")
    suspend fun deleteRegistration(
        @Path("registration_id") registrationId: Int
    ): Response<Map<String, Any>>
}
