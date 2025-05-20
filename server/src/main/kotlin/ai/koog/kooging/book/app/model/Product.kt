package ai.koog.kooging.book.app.model

import kotlinx.serialization.Serializable

/**
 * Represents a product in the product catalog.
 *
 * @property id The unique identifier of the product.
 * @property name The name of the product.
 * @property price The price of the product.
 */
@Serializable
data class Product(val id: Int, val name: String, val price: Double)