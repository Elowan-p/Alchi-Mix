package com.sdv.alchimix.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdv.alchimix.ui.theme.BrassGold
import com.sdv.alchimix.ui.theme.DarkBg
import com.sdv.alchimix.view.AlambicCyan
import com.sdv.alchimix.view.AlchemyBackground

@Composable
fun SplashScreenView(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        AlchemyBackground()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Le "Logo" animé
            Text(
                text = "⚗️",
                fontSize = 80.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "ALCHI-MIX",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 8.sp,
                    color = BrassGold
                )
            )

            Text(
                text = "Préparation du Laboratoire...",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            // Barre de chargement stylisée
            Column(modifier = Modifier.fillMaxWidth(0.8f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Extraction des essences API",
                        color = AlambicCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = AlambicCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AlambicCyan,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Cela peut prendre quelques secondes lors du premier lancement.",
                color = Color.Gray,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}