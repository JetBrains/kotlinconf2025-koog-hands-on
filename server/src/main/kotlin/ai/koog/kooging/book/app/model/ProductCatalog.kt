package ai.koog.kooging.book.app.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductCatalog(val products: List<Product>)
