package ai.koog.kooging.book.app.service

/**
 * Calculates the Levenshtein distance between two strings.
 *
 * @param query The first string to compare.
 * @param productName The second string to compare.
 * @return The Levenshtein distance between the two strings.
 */
fun levenshteinDistance(query: String, productName: String): Int {
    val m = query.length
    val n = productName.length
    val dp = Array(m + 1) { IntArray(n + 1) }

    for (i in 0..m) {
        dp[i][0] = i
    }
    for (j in 0..n) {
        dp[0][j] = j
    }

    for (i in 1..m) {
        for (j in 1..n) {
            dp[i][j] = if (query[i - 1] == productName[j - 1]) {
                dp[i - 1][j - 1]
            } else {
                minOf(
                    dp[i - 1][j] + 1,     // deletion
                    dp[i][j - 1] + 1,     // insertion
                    dp[i - 1][j - 1] + 1  // substitution
                )
            }
        }
    }

    return dp[m][n]
}
