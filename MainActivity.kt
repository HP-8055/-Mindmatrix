package com.example.nammaplatform

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nammaplatform.ui.theme.HappyBirthdayTheme
import com.example.nammaplatform.ui.theme.NammaBlue
import com.example.nammaplatform.ui.theme.NammaYellow
import java.util.*

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private var isTtsReady by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        setContent {
            HappyBirthdayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: TrainViewModel = viewModel()
                    NammaPlatformApp(
                        viewModel = viewModel,
                        onSpeak = { text ->
                            if (isTtsReady) {
                                tts.setSpeechRate(0.8f)
                                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("kn", "IN"))
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}

@Composable
fun NammaPlatformApp(viewModel: TrainViewModel, onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    val stations = listOf("KSR Bengaluru", "Yesvantpur", "Mysuru", "Hubballi", "Dharwad")
    var selectedStation by remember { mutableStateOf(stations[0]) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTrains(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ನಮ್ಮ ಪ್ಲಾಟ್‌ಫಾರ್ಮ್",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NammaBlue
                )
                Text(
                    text = "Namma Platform",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NammaBlue
                )
            }
            IconButton(onClick = { viewModel.loadTrains(context) }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = NammaBlue)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Station Selector
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            OutlinedCard(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = NammaYellow.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Station: $selectedStation", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                stations.forEach { station ->
                    DropdownMenuItem(
                        text = { Text(station) },
                        onClick = {
                            selectedStation = station
                            expanded = false
                        }
                    )
                }
            }
        }

        Text(
            text = "Next 3 Trains / ಮುಂದಿನ 3 ರೈಲುಗಳು",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NammaBlue,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (uiState) {
            is TrainUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NammaBlue)
                }
            }
            is TrainUiState.Success -> {
                val trains = (uiState as TrainUiState.Success).trains
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    trains.take(3).forEach { train ->
                        TrainCard(train = train, onSpeak = onSpeak)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            is TrainUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = (uiState as TrainUiState.Error).message, color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun TrainCard(train: Train, onSpeak: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = NammaBlue,
            contentColor = NammaYellow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = train.name, fontSize = 22.sp, fontWeight = FontWeight.Black, lineHeight = 26.sp)
                    Text(text = "No: ${train.id} • ${train.time}", fontSize = 16.sp, fontWeight = FontWeight.Normal)
                }
                Surface(
                    color = NammaYellow,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "PLATFORM", fontSize = 10.sp, color = NammaBlue, fontWeight = FontWeight.Bold)
                        Text(text = train.platform, fontSize = 32.sp, color = NammaBlue, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = "COACH POSITION (ಮಾರ್ಗದರ್ಶಿ):", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(train.coaches) { coach ->
                    CoachBox(name = coach)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { 
                    val announcement = "${train.name} ರೈಲು ಪ್ಲಾಟ್‌ಫಾರ್ಮ್ ಸಂಖ್ಯೆ ${train.platform} ಗೆ ಬರಲಿದೆ."
                    onSpeak(announcement)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NammaYellow,
                    contentColor = NammaBlue
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "ಕೇಳಿ (Speak in Kannada)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CoachBox(name: String) {
    val bgColor = when {
        name == "Engine" -> Color.Black
        name.contains("General") -> Color(0xFFE65100)
        name.contains("Ladies") -> Color(0xFFC2185B)
        name.startsWith("S") -> Color(0xFF1976D2)
        name.startsWith("B") || name.startsWith("A") -> Color(0xFF388E3C)
        else -> Color.DarkGray
    }
    
    Box(
        modifier = Modifier
            .size(width = 70.dp, height = 45.dp)
            .background(bgColor, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
