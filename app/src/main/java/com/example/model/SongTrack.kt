package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_history")
data class SongTrack(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val rawTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val artworkUrl: String? = null
) {
    val displayTitle: String
        get() = title.ifBlank { rawTitle.ifBlank { "Transmisión en Vivo" } }

    val displayArtist: String
        get() = artist.ifBlank { "Renuevo Radio" }
}
