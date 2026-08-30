package com.example.numberguesser

/**
 * Три НЕЗАВИСИМЫХ бинарных поиска, выполняемых параллельно —
 * по одному в каждой из трёх третей исходного диапазона [0, 9999].
 *
 * За один раунд берём по 1 числу из КАЖДОЙ трети (середину её текущего
 * оставшегося отрезка) — итого 3 числа за раз. Каждое из них тестируется
 * и убирается из своей трети независимо: обычное сужение low/high
 * пополам, как в классическом бинарном поиске, но параллельно в трёх
 * группах одновременно.
 *
 * Как только какая-то треть "схлопывается" (low > high) — значит,
 * искомого числа там нет, эта группа выбывает из дальнейших раундов,
 * и по факту в следующих раундах свои пробы дают только оставшиеся
 * активные группы.
 */
class ThreeWayParallelSearch(minValue: Int = 0, maxValue: Int = 9999) {

    enum class Cmp { LOWER, HIGHER, EQUAL }

    private data class Group(var low: Int, var high: Int, var active: Boolean = true)

    private val groups: Array<Group>
    private var rounds = 0
    private var totalChecks = 0

    init {
        val span = maxValue - minValue + 1
        val third = span / 3
        val b1 = minValue + third
        val b2 = minValue + 2 * third
        groups = arrayOf(
            Group(minValue, b1 - 1),
            Group(b1, b2 - 1),
            Group(b2, maxValue)
        )
    }

    /** Пробы текущего раунда: по одной от каждой ЕЩЁ активной группы (null, если группа выбыла). */
    fun nextRound(): Array<Int?> {
        rounds++
        return Array(3) { i ->
            val g = groups[i]
            if (g.active && g.low <= g.high) g.low + (g.high - g.low) / 2 else null
        }
    }

    /**
     * Применяет фидбек для каждой пробы этого раунда.
     * compare(groupIndex, guess) -> Cmp, вызывается только для непустых проб.
     * Возвращает найденное число, либо null — продолжать дальше.
     */
    fun applyRoundResult(guesses: Array<Int?>, compare: (Int, Int) -> Cmp): Int? {
        for (i in 0..2) {
            val guess = guesses[i] ?: continue
            totalChecks++
            when (compare(i, guess)) {
                Cmp.EQUAL -> return guess
                Cmp.LOWER -> groups[i].high = guess - 1   // target < guess, сужаем сверху
                Cmp.HIGHER -> groups[i].low = guess + 1   // target > guess, сужаем снизу
            }
            if (groups[i].low > groups[i].high) groups[i].active = false // группа исчерпана
        }
        return null
    }

    fun roundsCount(): Int = rounds
    fun totalChecksCount(): Int = totalChecks
    fun hasActiveGroups(): Boolean = groups.any { it.active && it.low <= it.high }

    /**
     * Форматирует пробы текущего раунда в строку вида "0166, 4999, 8333"
     * (каждое число — 4 цифры с ведущими нулями, выбывшие группы пропускаются).
     */
    fun formatRound(guesses: Array<Int?>): String {
        return guesses.filterNotNull()
            .joinToString(", ") { value ->
                val chars = CharArray(4)
                var v = value
                for (i in 3 downTo 0) {
                    chars[i] = ('0' + (v % 10))
                    v /= 10
                }
                String(chars)
            }
    }
}

/** Пример прогона: подставьте свою реализацию compare(), если фидбек берётся не из известного target. */
fun simulateThreeWay(target: Int): Pair<Int, Int> {
    require(target in 0..9999)
    val search = ThreeWayParallelSearch()
    while (true) {
        val guesses = search.nextRound()
        println("Раунд ${search.roundsCount()}: ${search.formatRound(guesses)}")
        val result = search.applyRoundResult(guesses) { _, guess ->
            when {
                target < guess -> ThreeWayParallelSearch.Cmp.LOWER
                target > guess -> ThreeWayParallelSearch.Cmp.HIGHER
                else -> ThreeWayParallelSearch.Cmp.EQUAL
            }
        }
        if (result != null) return search.roundsCount() to search.totalChecksCount()
        if (!search.hasActiveGroups()) error("Число не найдено — не должно происходить при валидном target")
    }
}
