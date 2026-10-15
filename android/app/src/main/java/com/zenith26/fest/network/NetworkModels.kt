package com.zenith26.fest.network

import com.google.gson.annotations.SerializedName

/**
 * Event data transfer object corresponding to backend EventOut/EventDetail.
 */
data class EventDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("event_date") val eventDate: String,
    @SerializedName("event_time") val eventTime: String,
    @SerializedName("venue") val venue: String,
    @SerializedName("capacity") val capacity: Int,
    @SerializedName("fee") val fee: Int = 0,
    @SerializedName("icon") val icon: String? = "📅",
    @SerializedName("available_seats") val availableSeats: Int
)

/**
 * Registration input sent from the client.
 */
data class RegistrationInDto(
    @SerializedName("student_name") val studentName: String,
    @SerializedName("roll_number") val rollNumber: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String? = null
)

/**
 * Registration confirmation response.
 */
data class RegistrationOutDto(
    @SerializedName("id") val id: Int,
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("student_name") val studentName: String,
    @SerializedName("registered_at") val registeredAt: String
)

/**
 * Full registration item displayed on the admin dashboard.
 */
data class RegistrationDetailDto(
    @SerializedName("id") val id: Int,
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("student_name") val studentName: String,
    @SerializedName("roll_number") val rollNumber: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("registered_at") val registeredAt: String,
    @SerializedName("event_title") val eventTitle: String
)

/**
 * Dashboard stats response.
 */
data class StatsDto(
    @SerializedName("total_events") val totalEvents: Int,
    @SerializedName("total_registrations") val totalRegistrations: Int,
    @SerializedName("total_capacity") val totalCapacity: Int,
    @SerializedName("overall_utilization_pct") val overallUtilizationPct: Double,
    @SerializedName("upcoming_events_count") val upcomingEventsCount: Int,
    @SerializedName("events_by_category") val eventsByCategory: Map<String, Int> = emptyMap()
)

/**
 * Admin create event request (stretch goal).
 */
data class EventCreateDto(
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String? = "",
    @SerializedName("event_date") val eventDate: String,
    @SerializedName("event_time") val eventTime: String,
    @SerializedName("venue") val venue: String,
    @SerializedName("capacity") val capacity: Int,
    @SerializedName("fee") val fee: Int = 0,
    @SerializedName("icon") val icon: String = "📅"
)

/**
 * FastAPI standard error format: {"detail": "..."} or {"detail": [{"msg": "..."}]}
 */
data class FastAPIError(
    @SerializedName("detail") val detail: Any?
)
