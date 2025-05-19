package ai.koog.kooging.book.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(val name: String, val id: String, val price: Double)
