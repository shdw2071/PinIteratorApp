package com.example.numberguesser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NumberGuesserScreen()
                }
            }
        }
    }
}

private fun formatNum(v: Int): String = v.toString().padStart(4, '0')

@Composable
fun NumberGuesserScreen() {
    var search by remember { mutableStateOf(ThreeWayParallelSearch()) }
    var guesses by remember { mutableStateOf(search.nextRound()) }
    val answers = remember { mutableStateMapOf<Int, ThreeWayParallelSearch.Cmp>() }
    var found by remember { mutableStateOf<Int?>(null) }
    var roundsDone by remember { mutableStateOf(0) }
    var totalChecks by remember { mutableStateOf(0) }

    fun restart() {
        search = ThreeWayParallelSearch()
        guesses = search.nextRound()
        answers.clear()
        found = null
        roundsDone = 0
        totalChecks = 0
    }

    fun submitIfReady() {
        val activeIndices = guesses.indices.filter { guesses[it] != null }
        if (activeIndices.isNotEmpty() && activeIndices.all { answers.containsKey(it) }) {
            val result = search.applyRoundResult(guesses) { i, _ -> answers[i]!! }
            roundsDone = search.roundsCount()
            totalChecks = search.totalChecksCount()
            answers.clear()
            if (result != null) {
                found = result
            } else {
                guesses = search.nextRound()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Параллельный поиск числа 0000-9999",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text("Раунд: $roundsDone   |   Проверок всего: $totalChecks")

        if (found != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Найдено: ${formatNum(found!!)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = { restart() }) {
                Text("Начать заново")
            }
        } else {
            Text(
                "Задайте ответ для каждого из чисел ниже относительно загаданного вами числа:",
                style = MaterialTheme.typography.bodyMedium
            )
            guesses.forEachIndexed { index, value ->
                if (value != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                formatNum(value),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        answers[index] = ThreeWayParallelSearch.Cmp.LOWER
                                        submitIfReady()
                                    }
                                ) { Text("Меньше") }
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        answers[index] = ThreeWayParallelSearch.Cmp.EQUAL
                                        submitIfReady()
                                    }
                                ) { Text("Угадал") }
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        answers[index] = ThreeWayParallelSearch.Cmp.HIGHER
                                        submitIfReady()
                                    }
                                ) { Text("Больше") }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = { restart() }) {
            Text("Сбросить")
        }
    }
}
