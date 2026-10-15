package com.zenith26.fest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.network.ApiClient
import com.zenith26.fest.network.ApiConfig
import com.zenith26.fest.network.EventDto
import com.zenith26.fest.ui.components.*
import com.zenith26.fest.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun StudentPortalScreen(
    onNavigateToAdmin: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    var events by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedEventForDetails by remember { mutableStateOf<EventDto?>(null) }

    // Fetch events function
    fun fetchEvents() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val catParam = if (selectedCategory.isBlank() || selectedCategory == "All") null else selectedCategory
                val searchParam = if (searchQuery.isBlank()) null else searchQuery.trim()

                val result = ApiClient.service.getEvents(
                    category = catParam,
                    search = searchParam
                )
                events = result
            } catch (e: Exception) {
                errorMessage = "Unable to connect to server at ${ApiConfig.baseUrl}\n(${e.localizedMessage ?: "Connection failed"})\n\nMake sure your FastAPI backend is running with:\nuvicorn backend.main:app --reload --port 5000"
            } finally {
                isLoading = false
            }
        }
    }

    // Trigger fetch on category change
    LaunchedEffect(selectedCategory) {
        fetchEvents()
    }

    Scaffold(
        topBar = {
            TopNavbar(
                isAdminScreen = false,
                onNavigateToggle = onNavigateToAdmin,
                onOpenSettings = onOpenSettings
            )
        },
        containerColor = Slate900
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Hero Header
            item {
                HeroSection()
            }

            // Controls: Search & Category Filter Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Explore Events",
                        color = Slate100,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Filter by category or search by event title",
                        color = Slate400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Search input box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            fetchEvents()
                        },
                        placeholder = { Text("Search events...", color = Slate500, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate400)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    fetchEvents()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate400)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100,
                            focusedBorderColor = Violet500,
                            unfocusedBorderColor = Slate700,
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Category Filter Chips
            item {
                CategoryFilterBar(
                    selectedCategory = selectedCategory,
                    onSelectCategory = { newCategory ->
                        selectedCategory = newCategory
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Event List States: Loading, Error, Empty, or Cards
            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Violet500)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "Loading events...", color = Slate400, fontSize = 13.sp)
                            }
                        }
                    }
                }

                errorMessage != null -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Rose400,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Connection Error",
                                    color = Rose400,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = Slate300,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { fetchEvents() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Violet600)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry Connection")
                                }
                            }
                        }
                    }
                }

                events.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "🔍", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No events match your search",
                                    color = Slate100,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try clearing the search query or selecting 'All' category.",
                                    color = Slate400,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = {
                                        searchQuery = ""
                                        selectedCategory = "All"
                                    },
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                                ) {
                                    Text("Reset Filters", color = Violet400)
                                }
                            }
                        }
                    }
                }

                else -> {
                    items(events, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            onViewDetails = { clickedEvent ->
                                selectedEventForDetails = clickedEvent
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet for viewing details and registering
    if (selectedEventForDetails != null) {
        EventDetailModal(
            event = selectedEventForDetails!!,
            onDismiss = { selectedEventForDetails = null },
            onRegistrationSuccess = {
                // Refresh events so available seats update immediately
                fetchEvents()
            }
        )
    }
}
