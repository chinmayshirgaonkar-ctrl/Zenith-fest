package com.zenith26.fest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zenith26.fest.network.ApiClient
import com.zenith26.fest.network.EventDto
import com.zenith26.fest.network.RegistrationInDto
import com.zenith26.fest.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EventDetailModal(
    event: EventDto,
    onDismiss: () -> Unit,
    onRegistrationSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var studentName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var clientError by remember { mutableStateOf<String?>(null) }
    var serverError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Slate700, RoundedCornerShape(24.dp)),
            color = Slate900
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header: Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = event.icon ?: "📅", fontSize = 24.sp)
                        Text(
                            text = event.title,
                            color = Slate100,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate400
                        )
                    }
                }

                Divider(color = Slate800, modifier = Modifier.padding(vertical = 12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    // Event Details Summary Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate850)
                            .border(1.dp, Slate750, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = event.description.takeIf { !it.isNullOrBlank() }
                                ?: "Join us for this exciting fest event! Seats are limited, so reserve your spot early.",
                            color = Slate300,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "📅 ${event.eventDate} ${event.eventTime}", color = Slate400, fontSize = 12.sp)
                            Text(text = "📍 ${event.venue}", color = Slate400, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Seats: ${event.availableSeats} of ${event.capacity} left",
                                color = if (event.availableSeats <= 0) Rose400 else Slate300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (event.fee > 0) "Fee: ₹${event.fee}" else "Fee: Free",
                                color = if (event.fee > 0) Amber400 else Emerald400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Registration Form Section Title
                    Text(
                        text = "Register for Event",
                        color = Slate100,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Please fill in your details to secure your registration.",
                        color = Slate400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Client validation error banner
                    if (clientError != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Rose950)
                                .border(1.dp, Rose500, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Rose400, modifier = Modifier.size(16.dp))
                            Text(text = clientError!!, color = Rose400, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Server error banner (e.g., "Event is full" or "Already registered")
                    if (serverError != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Rose950)
                                .border(1.dp, Rose500, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Rose400, modifier = Modifier.size(16.dp))
                            Text(text = serverError!!, color = Rose400, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Success banner
                    if (successMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Emerald950)
                                .border(1.dp, Emerald500, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald400, modifier = Modifier.size(16.dp))
                            Text(text = successMessage!!, color = Emerald400, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Form Fields
                    // 1. Student Name
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = {
                            studentName = it
                            clientError = null
                            serverError = null
                        },
                        label = { Text("Full Name *", color = Slate400, fontSize = 12.sp) },
                        placeholder = { Text("e.g. Maya Sharma", color = Slate600, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100,
                            focusedBorderColor = Violet500,
                            unfocusedBorderColor = Slate700,
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Roll Number
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = {
                            rollNumber = it
                            clientError = null
                            serverError = null
                        },
                        label = { Text("Roll Number / Student ID *", color = Slate400, fontSize = 12.sp) },
                        placeholder = { Text("e.g. CS2026-042", color = Slate600, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100,
                            focusedBorderColor = Violet500,
                            unfocusedBorderColor = Slate700,
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            clientError = null
                            serverError = null
                        },
                        label = { Text("College Email *", color = Slate400, fontSize = 12.sp) },
                        placeholder = { Text("e.g. maya@college.edu", color = Slate600, fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100,
                            focusedBorderColor = Violet500,
                            unfocusedBorderColor = Slate700,
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Phone (Optional)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number (Optional)", color = Slate400, fontSize = 12.sp) },
                        placeholder = { Text("e.g. 9876543210", color = Slate600, fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100,
                            focusedBorderColor = Violet500,
                            unfocusedBorderColor = Slate700,
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            // Client-side validations
                            if (studentName.isBlank()) {
                                clientError = "Please enter your name."
                                return@Button
                            }
                            if (rollNumber.isBlank()) {
                                clientError = "Please enter your roll number."
                                return@Button
                            }
                            if (email.isBlank() || !email.contains("@")) {
                                clientError = "Please enter a valid email address containing '@'."
                                return@Button
                            }

                            clientError = null
                            serverError = null
                            isSubmitting = true

                            coroutineScope.launch {
                                try {
                                    val response = ApiClient.service.registerForEvent(
                                        eventId = event.id,
                                        body = RegistrationInDto(
                                            studentName = studentName.trim(),
                                            rollNumber = rollNumber.trim(),
                                            email = email.trim(),
                                            phone = phone.trim().takeIf { it.isNotBlank() }
                                        )
                                    )

                                    if (response.isSuccessful) {
                                        successMessage = "Registered successfully for ${event.title}!"
                                        onRegistrationSuccess()
                                        // Wait a moment so user sees confirmation, then dismiss
                                        delay(1500)
                                        onDismiss()
                                    } else {
                                        serverError = ApiClient.parseErrorMessage(response)
                                    }
                                } catch (e: Exception) {
                                    serverError = "Failed to connect to backend: ${e.localizedMessage ?: "Unknown error"}"
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = !isSubmitting && event.availableSeats > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Violet600,
                            contentColor = White,
                            disabledContainerColor = Slate700,
                            disabledContentColor = Slate500
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (event.availableSeats <= 0) "Event is Full" else "Confirm Registration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

val Slate750 = Color(0xFF283548)
