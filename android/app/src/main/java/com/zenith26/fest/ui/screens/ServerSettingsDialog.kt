package com.zenith26.fest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zenith26.fest.network.ApiConfig
import com.zenith26.fest.ui.theme.*

@Composable
fun ServerSettingsDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var urlText by remember { mutableStateOf(ApiConfig.baseUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Slate700, RoundedCornerShape(20.dp)),
            color = Slate900
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "⚙️ Backend Server Settings",
                    color = Slate100,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Configure the FastAPI host URL. Use 10.0.2.2 for Android Emulator, or your computer's Wi-Fi IP for physical phones.",
                    color = Slate400,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("Base URL", color = Slate400, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100,
                        focusedBorderColor = Violet500,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Presets
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(
                        onClick = { urlText = "https://zenith-fest-39rq.vercel.app/" },
                        label = { Text("🚀 Live Vercel Cloud (Permanent)", fontSize = 11.sp, color = Slate300) }
                    )
                    AssistChip(
                        onClick = { urlText = "https://another-kinase-emerald-football.trycloudflare.com/" },
                        label = { Text("🌐 Cloudflare Tunnel", fontSize = 11.sp, color = Slate300) }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { urlText = "http://172.16.58.160:5000/" },
                            label = { Text("Wi-Fi (172.16.58.160)", fontSize = 11.sp, color = Slate300) }
                        )
                        AssistChip(
                            onClick = { urlText = "http://10.0.2.2:5000/" },
                            label = { Text("Emulator (10.0.2.2)", fontSize = 11.sp, color = Slate300) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Slate400)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            ApiConfig.updateBaseUrl(urlText)
                            onSave()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Violet600)
                    ) {
                        Text("Save & Connect")
                    }
                }
            }
        }
    }
}
