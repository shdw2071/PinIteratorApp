package com.example.piniterator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PinScreen()
            }
        }
    }
}

@Composable
fun PinScreen(viewModel: MainViewModel = viewModel()) {
    var pin by remember { mutableStateOf("Натисніть Далі") }
    var progress by remember { mutableStateOf("0/10000") }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.pin.observeForever { value ->
            pin = value ?: "Натисніть Далі"
        }
        viewModel.progress.observeForever { value ->
            progress = value
        }
        viewModel.finished.observeForever { value ->
            finished = value
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = pin, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Прогрес: $progress")
        Spacer(modifier = Modifier.height(32.dp))
        Row {
            Button(onClick = { viewModel.onNext() }, enabled = !finished) {
                Text("Далі")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { viewModel.onReset() }) {
                Text("Скинути")
            }
        }
    }
}
