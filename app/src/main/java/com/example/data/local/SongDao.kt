package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.SongTrack
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM song_history ORDER BY timestamp DESC")
    fun getAllSongs(): Flow<List<SongTrack>>

    @Query("SELECT * FROM song_history WHERE rawTitle = :rawTitle ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestByRawTitle(rawTitle: String): SongTrack?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongTrack): Long

    @Query("UPDATE song_history SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM song_history WHERE id = :id")
    suspend fun deleteSong(id: Long)

    @Query("DELETE FROM song_history")
    suspend fun clearHistory()

    @Query("SELECT COUNT(*) FROM song_history")
    suspend fun getCount(): Int
}
