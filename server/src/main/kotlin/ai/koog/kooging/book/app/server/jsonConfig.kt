package ai.koog.kooging.book.app.server

import kotlinx.serialization.json.Json

val defaultJson: Json = Json {
    prettyPrint = true
    isLenient = true
}