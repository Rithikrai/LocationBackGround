package com.example.locationnew.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Locations")
data class Locations(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val lat: String,
    val longi: String
)