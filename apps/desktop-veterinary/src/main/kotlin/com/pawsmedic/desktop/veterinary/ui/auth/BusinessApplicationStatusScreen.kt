package com.pawsmedic.desktop.veterinary.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.desktop.veterinary.BusinessApplicationRecord
import com.pawsmedic.desktop.veterinary.ui.theme.*

@Composable
fun BusinessApplicationStatusScreen(
    application: BusinessApplicationRecord,
    isRejected: Boolean,
    isRefreshing: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBg)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 620.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isRejected) Icons.Default.Verified else Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = if (isRejected) Color(0xFFB91C1C) else ClinicalEmerald,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = if (isRejected) "Solicitud no aprobada" else "Solicitud pendiente",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isRejected) {
                        "La solicitud de ${application.businessName} fue rechazada. Contacta al equipo de PawsMedic para recibir orientación."
                    } else {
                        "Recibimos la solicitud de ${application.businessName}. El Superadmin debe revisarla antes de habilitar la Agenda y las funciones clínicas."
                    },
                    fontSize = 15.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Estado: ${application.status} · ${application.municipality}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 13.sp,
                        color = Color(0xFFB91C1C)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                if (!isRejected) {
                    Button(
                        onClick = onRefresh,
                        enabled = !isRefreshing,
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        }
                        Text(if (isRefreshing) "Verificando..." else "Verificar estado")
                    }
                }
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
    }
}
