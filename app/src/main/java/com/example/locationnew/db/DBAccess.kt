package com.example.locationnew.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DBAccess {
    @Insert
    suspend fun insertAll(locations: Locations)

    @Query("Select * From locations order by id ASC")
    fun getContact(): LiveData<List<Locations>>
}