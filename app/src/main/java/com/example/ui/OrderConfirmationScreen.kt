package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.SlateDark
import kotlinx.coroutines.delay

@Composable
fun OrderConfirmationScreen(
    onHome: () -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var showFeedbackSuccess by remember { mutableStateOf(false) }
    var showNotification by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(2500) // Simulate delay for "order received" feel
        showNotification = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = AmberGold
            )
            Text(
                "Order Placed!",
                style = MaterialTheme.typography.headlineLarge,
                color = AmberGold,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Your delicious Lucknawi Chai is being prepared.",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )

            if (showNotification) {
                Spacer(modifier = Modifier.height(24.dp))

                // Feedback Section appearing as a "notification" prompt
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateDark),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Order Received? Rate Us!",
                            color = AmberGold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (!showFeedbackSuccess) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                (1..5).forEach { index ->
                                    IconButton(onClick = { rating = index }) {
                                        Icon(
                                            imageVector = if (index <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Rate $index stars",
                                            tint = if (index <= rating) AmberGold else Color.Gray,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                            
                            if (rating > 0) {
                                Button(
                                    onClick = { 
                                        showFeedbackSuccess = true
                                        Toast.makeText(context, "Thank you for your $rating star feedback!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Submit Feedback", color = DeepBlack)
                                }
                            }
                        } else {
                            Text(
                                "Thank you! We've received your feedback. 🙏",
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onHome,
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold.copy(alpha = 0.2f)),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(AmberGold, AmberGold))),
            ) {
                Text("Back to Menu", color = AmberGold)
            }
        }
    }
}
