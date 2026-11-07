package com.example.data.remote

import android.util.Log
import com.example.model.BibleVerse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BibleVerseService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()
) {
    private val tag = "BibleVerseService"

    private val spanishBookNames = mapOf(
        1 to "Génesis", 2 to "Éxodo", 3 to "Levítico", 4 to "Números", 5 to "Deuteronomio",
        6 to "Josué", 7 to "Jueces", 8 to "Rut", 9 to "1 Samuel", 10 to "2 Samuel",
        11 to "1 Reyes", 12 to "2 Reyes", 13 to "1 Crónicas", 14 to "2 Crónicas", 15 to "Esdras",
        16 to "Nehemías", 17 to "Ester", 18 to "Job", 19 to "Salmos", 20 to "Proverbios",
        21 to "Eclesiastés", 22 to "Cantares", 23 to "Isaías", 24 to "Jeremías", 25 to "Lamentaciones",
        26 to "Ezequiel", 27 to "Daniel", 28 to "Oseas", 29 to "Joel", 30 to "Amós",
        31 to "Abdías", 32 to "Jonás", 33 to "Miqueas", 34 to "Nahúm", 35 to "Habacuc",
        36 to "Sofonías", 37 to "Hageo", 38 to "Zacarías", 39 to "Malaquías", 40 to "Mateo",
        41 to "Marcos", 42 to "Lucas", 43 to "Juan", 44 to "Hechos", 45 to "Romanos",
        46 to "1 Corintios", 47 to "2 Corintios", 48 to "Gálatas", 49 to "Efesios", 50 to "Filipenses",
        51 to "Colosenses", 52 to "1 Tesalonicenses", 53 to "2 Tesalonicenses", 54 to "1 Timoteo",
        55 to "2 Timoteo", 56 to "Tito", 57 to "Filemón", 58 to "Hebreos", 59 to "Santiago",
        60 to "1 Pedro", 61 to "2 Pedro", 62 to "1 Juan", 63 to "2 Juan", 64 to "3 Juan",
        65 to "Judas", 66 to "Apocalipsis"
    )

    // Curated daily verses in Reina-Valera 1960 (RVR1960)
    private val curatedDailyVerses = listOf(
        BibleVerse(
            reference = "Filipenses 4:13",
            text = "Todo lo puedo en Cristo que me fortalece.",
            version = "RVR1960",
            theme = "Fortaleza y Victoria",
            bookId = 50, chapter = 4, verse = 13
        ),
        BibleVerse(
            reference = "Jeremías 29:11",
            text = "Porque yo sé los pensamientos que tengo acerca de vosotros, dice Jehová, pensamientos de paz, y no de mal, para daros el fin que esperáis.",
            version = "RVR1960",
            theme = "Esperanza y Futuro",
            bookId = 24, chapter = 29, verse = 11
        ),
        BibleVerse(
            reference = "Salmos 23:1",
            text = "Jehová es mi pastor; nada me faltará.",
            version = "RVR1960",
            theme = "Provisión y Cuidado",
            bookId = 19, chapter = 23, verse = 1
        ),
        BibleVerse(
            reference = "Isaías 40:31",
            text = "Pero los que esperan a Jehová tendrán nuevas fuerzas; levantarán alas como las águilas; correrán, y no se cansarán; caminarán, y no se fatigarán.",
            version = "RVR1960",
            theme = "Renuevo Espiritual",
            bookId = 23, chapter = 40, verse = 31
        ),
        BibleVerse(
            reference = "Josué 1:9",
            text = "Mira que te mando que te esfuerces y seas valiente; no temas ni desmayes, porque Jehová tu Dios estará contigo en dondequiera que vayas.",
            version = "RVR1960",
            theme = "Valentía y Ánimo",
            bookId = 6, chapter = 1, verse = 9
        ),
        BibleVerse(
            reference = "Romanos 8:28",
            text = "Y sabemos que a los que aman a Dios, todas las cosas les ayudan a bien, esto es, a los que conforme a su propósito son llamados.",
            version = "RVR1960",
            theme = "Propósito Divino",
            bookId = 45, chapter = 8, verse = 28
        ),
        BibleVerse(
            reference = "Proverbios 3:5-6",
            text = "Fíate de Jehová de todo tu corazón, y no te apoyes en tu propia prudencia. Reconócelo en todos tus caminos, y él enderezará tus veredas.",
            version = "RVR1960",
            theme = "Confianza y Guía",
            bookId = 20, chapter = 3, verse = 5
        ),
        BibleVerse(
            reference = "Salmos 46:1",
            text = "Dios es nuestro amparo y fortaleza, nuestro pronto auxilio en las tribulaciones.",
            version = "RVR1960",
            theme = "Protección y Refugio",
            bookId = 19, chapter = 46, verse = 1
        ),
        BibleVerse(
            reference = "Juan 3:16",
            text = "Porque de tal manera amó Dios al mundo, que ha dado a su Hijo unigénito, para que todo aquel que en él cree, no se pierda, mas tenga vida eterna.",
            version = "RVR1960",
            theme = "Amor y Salvación",
            bookId = 43, chapter = 3, verse = 16
        ),
        BibleVerse(
            reference = "Mateo 11:28",
            text = "Venid a mí todos los que estáis trabajados y cargados, y yo os haré descansar.",
            version = "RVR1960",
            theme = "Paz y Descanso",
            bookId = 40, chapter = 11, verse = 28
        ),
        BibleVerse(
            reference = "Salmos 121:1-2",
            text = "Alzaré mis ojos a los montes; ¿De dónde vendrá mi socorro? Mi socorro viene de Jehová, que hizo los cielos y la tierra.",
            version = "RVR1960",
            theme = "Socorro Celestial",
            bookId = 19, chapter = 121, verse = 1
        ),
        BibleVerse(
            reference = "Gálatas 5:22-23",
            text = "Mas el fruto del Espíritu es amor, gozo, paz, paciencia, benignidad, bondad, fe, mansedumbre, templanza; contra tales cosas no hay ley.",
            version = "RVR1960",
            theme = "Fruto del Espíritu",
            bookId = 48, chapter = 5, verse = 22
        ),
        BibleVerse(
            reference = "2 Corintios 5:17",
            text = "De modo que si alguno está en Cristo, nueva criatura es; las cosas viejas pasaron; he aquí todas son hechas nuevas.",
            version = "RVR1960",
            theme = "Vida Nueva en Cristo",
            bookId = 47, chapter = 5, verse = 17
        ),
        BibleVerse(
            reference = "Salmos 119:105",
            text = "Lámpara es a mis pies tu palabra, y lumbrera a mi camino.",
            version = "RVR1960",
            theme = "Luz y Sabiduría",
            bookId = 19, chapter = 119, verse = 105
        ),
        BibleVerse(
            reference = "Filipenses 4:6-7",
            text = "Por nada estéis afanosos, sino sean conocidas vuestras peticiones delante de Dios en toda oración y ruego, con acción de gracias. Y la paz de Dios, que sobrepasa todo entendimiento, guardará vuestros corazones y vuestros pensamientos en Cristo Jesús.",
            version = "RVR1960",
            theme = "Oración y Paz de Dios",
            bookId = 50, chapter = 4, verse = 6
        )
    )

    /**
     * Returns today's Verse of the Day in RVR1960.
     * First attempts to verify/fetch from the live Bolls Life RVR1960 API.
     * Falls back seamlessly to the day-indexed curated RVR1960 verse.
     */
    suspend fun getVerseOfTheDay(): BibleVerse = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val defaultVerse = curatedDailyVerses[dayOfYear % curatedDailyVerses.size]

        // Fetch live text from API for this specific verse reference
        if (defaultVerse.bookId > 0 && defaultVerse.chapter > 0 && defaultVerse.verse > 0) {
            val apiVerse = fetchSpecificVerse(
                bookId = defaultVerse.bookId,
                chapter = defaultVerse.chapter,
                verse = defaultVerse.verse,
                fallbackTheme = defaultVerse.theme
            )
            if (apiVerse != null) {
                return@withContext apiVerse
            }
        }

        return@withContext defaultVerse
    }

    /**
     * Fetches a fresh random verse from Bolls Life API in RVR1960.
     */
    suspend fun getRandomVerse(): BibleVerse = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://bolls.life/get-random-verse/RVR1960/")
                .header("User-Agent", "RenuevoRadio/2.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                response.close()
                if (!body.isNullOrBlank()) {
                    val json = JSONObject(body)
                    val rawText = json.optString("text", "")
                    val cleanText = sanitizeVerseText(rawText)
                    val bookNum = json.optInt("book", 0)
                    val chapter = json.optInt("chapter", 0)
                    val verseNum = json.optInt("verse", 0)

                    val bookName = spanishBookNames[bookNum] ?: "Libro $bookNum"
                    val ref = "$bookName $chapter:$verseNum"

                    if (cleanText.isNotBlank()) {
                        return@withContext BibleVerse(
                            reference = ref,
                            text = cleanText,
                            version = "RVR1960",
                            theme = "Palabra Inspirada",
                            bookId = bookNum,
                            chapter = chapter,
                            verse = verseNum
                        )
                    }
                }
            } else {
                response.close()
            }
        } catch (e: Exception) {
            Log.w(tag, "Random verse API failed: ${e.message}")
        }

        // Random fallback from curated list
        return@withContext curatedDailyVerses.random()
    }

    private fun fetchSpecificVerse(
        bookId: Int,
        chapter: Int,
        verse: Int,
        fallbackTheme: String
    ): BibleVerse? {
        try {
            val url = "https://bolls.life/get-verse/RVR1960/$bookId/$chapter/$verse/"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "RenuevoRadio/2.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                response.close()
                if (!body.isNullOrBlank()) {
                    val json = JSONObject(body)
                    val rawText = json.optString("text", "")
                    val cleanText = sanitizeVerseText(rawText)
                    val bookName = spanishBookNames[bookId] ?: "Libro $bookId"
                    val ref = "$bookName $chapter:$verse"

                    if (cleanText.isNotBlank()) {
                        return BibleVerse(
                            reference = ref,
                            text = cleanText,
                            version = "RVR1960",
                            theme = fallbackTheme,
                            bookId = bookId,
                            chapter = chapter,
                            verse = verse
                        )
                    }
                }
            } else {
                response.close()
            }
        } catch (e: Exception) {
            Log.w(tag, "Specific verse API call failed ($bookId $chapter:$verse): ${e.message}")
        }
        return null
    }

    private fun sanitizeVerseText(rawText: String): String {
        return rawText
            .replace(Regex("<[^>]*>"), "") // strip HTML tags
            .replace(Regex("\\[.*?\\]"), "") // strip footnotes
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
