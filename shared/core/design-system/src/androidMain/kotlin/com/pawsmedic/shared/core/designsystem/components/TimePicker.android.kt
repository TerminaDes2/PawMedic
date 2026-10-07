package com.pawsmedic.shared.core.designsystem.components

import android.view.LayoutInflater
import android.widget.TimePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.pawsmedic.shared.core.designsystem.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
actual fun TimePickerModal(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (hour: Int, minute: Int, formattedTime: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar Hora",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                AndroidView(
                    factory = { context ->
                        val view = LayoutInflater.from(context).inflate(R.layout.spinner_time_picker, null)
                        val timePicker = view.findViewById<TimePicker>(R.id.nativeTimePicker)

                        timePicker.setIs24HourView(false)
                        timePicker.hour = initialHour
                        timePicker.minute = initialMinute

                        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
                            selectedHour = hourOfDay
                            selectedMinute = minute
                        }

                        view
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val time = LocalTime.of(selectedHour, selectedMinute)
                            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
                            val formatted = time.format(formatter)

                            onTimeSelected(selectedHour, selectedMinute, formatted)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00897B)
                        )
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}
