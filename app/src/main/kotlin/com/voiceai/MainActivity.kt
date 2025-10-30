package com.voiceai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Main Activity for Voice AI Android Application
 *
 * TODO: This is a minimal placeholder. Implement full voice AI functionality:
 * - Voice recording with STT
 * - Intent classification with Gemini Nano
 * - LLM inference with Llama 3.2
 * - TTS with Cartesia Sonic 3
 * - Emotion detection and response
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlaceholderScreen()
                }
            }
        }
    }

    @Composable
    fun PlaceholderScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎤 Voice AI Android",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hybrid SLM Architecture",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Architecture Components:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✅ Gemini Nano (on-device)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "✅ Llama 3.2 (ARM backend)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "✅ Cartesia Sonic 3 (TTS)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "✅ Emotion Detection",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "✅ Skills Framework",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Status: Foundation Complete ✅\nImplementation: In Progress 🚧",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "See /docs/SONIC3_QUICKSTART.md\nto implement Sonic 3 TTS",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
