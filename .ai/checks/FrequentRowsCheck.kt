// Standalone check for lawnchair/src/app/lawnchair/predictions/FrequentRows.kt, run by .ai/bin/check.
import app.lawnchair.predictions.frequentSuggestionCount

fun expect(actual: Int, expected: Int, case: String) =
    check(actual == expected) { "$case: expected $expected, got $actual" }

fun main() {
    val c = 4
    expect(frequentSuggestionCount(listOf(9, 8, 7, 6), c), 4, "exactly one row")
    expect(frequentSuggestionCount(listOf(5, 2), c), 2, "fewer than one row")
    expect(frequentSuggestionCount(listOf(20, 18, 15, 12, 10, 9, 8, 7, 7, 6, 6, 6, 3), c), 12, "two extra rows (6 = half of 12 qualifies, 3 does not)")
    expect(frequentSuggestionCount(listOf(20, 18, 15, 12, 10, 9, 8, 7, 5, 5), c), 8, "5 is below half of 12 -> one extra row")
    expect(frequentSuggestionCount(listOf(20, 18, 15, 12, 10, 9, 8), c), 4, "partial extra row is dropped")
    expect(frequentSuggestionCount(listOf(9, 8, 7, 0, 9, 9, 9, 9), c), 4, "row 1 padded with fallback -> no extras")
    expect(frequentSuggestionCount(listOf(4, 4, 4, 4, 2, 2, 2, 2), c), 4, "below the 3-launch floor")
    expect(frequentSuggestionCount(List(40) { 50 }, c), 12, "capped at 2 extra rows")
    expect(frequentSuggestionCount(listOf(9, 8), 0), 0, "no columns")
    println("frequent rows: ok")
}
