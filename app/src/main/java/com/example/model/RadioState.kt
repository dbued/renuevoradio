package com.example.model

enum class PlaybackStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

enum class AppThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

data class StreamInfo(
    val stationName: String = "RENUEVO RADIO",
    val frequency: String = "102.4 FM",
    val slogan: String = "Viviendo en su Presencia",
    val genre: String = "Música Cristiana Contemporánea",
    val bitrate: String = "96 kbps",
    val codec: String = "AAC Stereo",
    val streamUrl: String = "https://stream-177.surfernetwork.com/f6ks13hmdwzuv?zt=eyJhbGciOiJIUzI1NiJ9.eyJzdHJlYW0iOiJmNmtzMTNobWR3enV2IiwiaG9zdCI6InN0cmVhbS0xNzcuc3VyZmVybmV0d29yay5jb20iLCJydHRsIjo1LCJqdGkiOiJUdFZDOHBiNlRmS0ZHbGczb2NOc0xRIiwiaWF0IjoxNzkyNzIxNDEzLCJleHAiOjE3OTI3MjE0NzN9.92MMgsJqFnPtVUvvwXPs4e_bCmM6iChePetajsRP9yo",
    val fallbackUrl: String = "https://stream.surfernetwork.com/f6ks13hmdwzuv"
)
