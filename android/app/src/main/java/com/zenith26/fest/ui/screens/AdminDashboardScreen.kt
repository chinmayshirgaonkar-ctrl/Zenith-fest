package com.zenith26.fest.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.network.*
import com.zenith26.fest.ui.components.*
import com.zenith26.fest.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToStudentPortal: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var stats by remember { mutableStateOf<StatsDto?>(null) }
    var events by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var registrations by remember { mutableStateOf<List<RegistrationDetailDto>>(emptyList()) }

    var selectedEventFilterId by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Tab state: "Overview & Events" vs "All Registrations"
    var activeTab by remember { mutableIntStateOf(0) }

    // Dialog states for stretch goals
    var registrationToDelete by remember { mutableStateOf<RegistrationDetailDto?>(null) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    fun refreshAllData() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                stats = ApiClient.service.getStats()
                val rawEvents = ApiClient.service.getEvents(null, null)
                // Sort events by utilization % descending
                events = rawEvents.sortedByDescending { ev ->
                    if (ev.capacity > 0) ((ev.capacity - ev.availableSeats).toDouble() / ev.capacity.toDouble()) * 100.0 else 0.0
                }
                registrations = ApiClient.service.getRegistrations(selectedEventFilterId)
            } catch (e: Exception) {
                errorMessage = "Failed to load admin data: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedEventFilterId) {
        refreshAllData()
    }

    Scaffold(
        topBar = {
            TopNavbar(
                isAdminScreen = true,
                onNavigateToggle = onNavigateToStudentPortal,
                onOpenSettings = onOpenSettings
            )
        },
        floatingActionButton = {
            if (activeTab == 0) {
                FloatingActionButton(
                    onClick = { showAddEventDialog = true },
                    containerColor = Violet600,
                    contentColor = White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            }
        },
        containerColor = Slate900
    ) { paddingValues ->
        if (isLoading && stats == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Violet500)
            }
        } else if (errorMessage != null && stats == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = errorMessage!!, color = Rose400, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { refreshAllData() }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Header & Live Badge
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Admin Control Center",
                                color = Slate100,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Real-time registration statistics & seat telemetry",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(onClick = { refreshAllData() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Violet400)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4 Top Stat Cards (2x2 Grid)
                stats?.let { s ->
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatCard(
                                    title = "TOTAL EVENTS",
                                    value = "${s.totalEvents}",
                                    subtitle = "Active in fest",
                                    icon = "🏆",
                                    accentColor = Violet500,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "REGISTRATIONS",
                                    value = "${s.totalRegistrations}",
                                    subtitle = "Confirmed seats",
                                    icon = "🎟️",
                                    accentColor = Emerald500,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatCard(
                                    title = "UTILIZATION",
                                    value = "${s.overallUtilizationPct}%",
                                    subtitle = "${s.totalRegistrations} / ${s.totalCapacity} total cap",
                                    icon = "📊",
                                    accentColor = Amber500,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "UPCOMING",
                                    value = "${s.upcomingEventsCount}",
                                    subtitle = "Starting Nov 14",
                                    icon = "📅",
                                    accentColor = Cyan500,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // Category Breakdown Cards
                    item {
                        Text(
                            text = "Events by Category",
                            color = Slate100,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate850)
                                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            s.eventsByCategory.forEach { (cat, count) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "$count", color = Violet400, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text(text = cat, color = Slate400, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }

                // Tab Switcher (Events vs Registrations)
                item {
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = Slate850,
                        contentColor = Violet400,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text("Events Utilization (${events.size})", fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text("Registrations (${registrations.size})", fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Tab 0: Event Utilization Table-like Cards (sorted desc by %)
                if (activeTab == 0) {
                    items(events, key = { it.id }) { ev ->
                        val registered = ev.capacity - ev.availableSeats
                        val utilPct = if (ev.capacity > 0) ((registered.toDouble() / ev.capacity.toDouble()) * 100.0) else 0.0
                        val isOver90 = utilPct >= 90.0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isOver90) Amber500 else Slate700
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = ev.icon ?: "📅", fontSize = 18.sp)
                                        Text(
                                            text = ev.title,
                                            color = Slate100,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Utilization % Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isOver90) Amber950 else Violet950)
                                            .border(1.dp, if (isOver90) Amber500 else Violet600, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "${String.format("%.1f", utilPct)}%",
                                            color = if (isOver90) Amber400 else Violet400,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Category: ${ev.category}", color = Slate400, fontSize = 12.sp)
                                    Text(text = "Venue: ${ev.venue}", color = Slate400, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Registered: $registered / ${ev.capacity}",
                                        color = if (isOver90) Amber400 else Slate300,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (isOver90) {
                                        Text(
                                            text = "⚠️ HIGH DEMAND (>90%)",
                                            color = Amber400,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 1: Raw Registration List with Event Filter
                if (activeTab == 1) {
                    item {
                        // Event Filter Dropdown
                        var expanded by remember { mutableStateOf(false) }
                        val currentFilterName = events.find { it.id == selectedEventFilterId }?.title ?: "All Events"

                        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Slate700),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate100)
                            ) {
                                Text("Filter: $currentFilterName", fontSize = 13.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Slate800)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Events", color = Slate100) },
                                    onClick = {
                                        selectedEventFilterId = null
                                        expanded = false
                                    }
                                )
                                events.forEach { ev ->
                                    DropdownMenuItem(
                                        text = { Text("${ev.icon} ${ev.title}", color = Slate100) },
                                        onClick = {
                                            selectedEventFilterId = ev.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (registrations.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No registrations yet.",
                                    color = Slate400,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(registrations, key = { it.id }) { reg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate800),
                                border = BorderStroke(1.dp, Slate700)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = reg.studentName,
                                            color = Slate100,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${reg.rollNumber} • ${reg.email}",
                                            color = Slate400,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Event: ${reg.eventTitle}",
                                            color = Violet400,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Time: ${reg.registeredAt}",
                                            color = Slate500,
                                            fontSize = 10.sp
                                        )
                                    }

                                    // Cancel registration button (Stretch Goal)
                                    IconButton(
                                        onClick = { registrationToDelete = reg }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Cancel Registration",
                                            tint = Rose400,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Cancelling Registration (Stretch Goal)
    if (registrationToDelete != null) {
        AlertDialog(
            onDismissRequest = { registrationToDelete = null },
            title = { Text("Cancel Registration?", color = Slate100, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to cancel the registration for ${registrationToDelete!!.studentName} for '${registrationToDelete!!.eventTitle}'? This will free up 1 seat.",
                    color = Slate300,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toDelete = registrationToDelete!!
                        registrationToDelete = null
                        coroutineScope.launch {
                            try {
                                ApiClient.service.deleteRegistration(toDelete.id)
                                refreshAllData()
                            } catch (e: Exception) {
                                // Error handling
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("Cancel Registration")
                }
            },
            dismissButton = {
                TextButton(onClick = { registrationToDelete = null }) {
                    Text("Keep", color = Slate400)
                }
            },
            containerColor = Slate850,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Add New Event Dialog (Stretch Goal)
    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onEventCreated = {
                showAddEventDialog = false
                refreshAllData()
            }
        )
    }
}

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onEventCreated: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technical") }
    var description by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("2026-11-14") }
    var eventTime by remember { mutableStateOf("10:00 AM") }
    var venue by remember { mutableStateOf("") }
    var capacityText by remember { mutableStateOf("50") }
    var feeText by remember { mutableStateOf("0") }
    var icon by remember { mutableStateOf("⚡") }

    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Fest Event", color = Slate100, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = Rose400, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Technical, Cultural, etc.) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = capacityText,
                        onValueChange = { capacityText = it },
                        label = { Text("Capacity *") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = feeText,
                        onValueChange = { feeText = it },
                        label = { Text("Fee (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacityText.toIntOrNull() ?: 0
                    val fee = feeText.toIntOrNull() ?: 0
                    if (title.isBlank() || venue.isBlank() || cap <= 0) {
                        errorMsg = "Please fill all required fields and set capacity > 0."
                        return@Button
                    }
                    isSubmitting = true
                    coroutineScope.launch {
                        try {
                            val res = ApiClient.service.createEvent(
                                EventCreateDto(
                                    title = title.trim(),
                                    category = category.trim(),
                                    description = description.trim(),
                                    eventDate = eventDate.trim(),
                                    eventTime = eventTime.trim(),
                                    venue = venue.trim(),
                                    capacity = cap,
                                    fee = fee,
                                    icon = icon.trim()
                                )
                            )
                            if (res.isSuccessful) {
                                onEventCreated()
                            } else {
                                errorMsg = ApiClient.parseErrorMessage(res)
                            }
                        } catch (e: Exception) {
                            errorMsg = "Error: ${e.localizedMessage}"
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Violet600)
            ) {
                Text("Create Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Slate400) }
        },
        containerColor = Slate850,
        shape = RoundedCornerShape(16.dp)
    )
}
