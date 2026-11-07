package com.example.model

data class BibleVerse(
    val reference: String,
    val text: String,
    val version: String = "RVR1960",
    val theme: String = "Promesa de Dios",
    val bookId: Int = 0,
    val chapter: Int = 0,
    val verse: Int = 0
) {
    val formattedShareText: String
        get() = """
            «$text»
            — $reference ($version)
            
            Sintoniza Renuevo Radio • Life Turned Up!
            https://stream.surfernetwork.com/f6ks13hmdwzuv
        """.trimIndent()
}
