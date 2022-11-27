package com.example.locationnew.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Locations::class], version = 1, exportSchema = false)
abstract class MainDatabase :RoomDatabase() {

    abstract  fun  locaton():DBAccess

}