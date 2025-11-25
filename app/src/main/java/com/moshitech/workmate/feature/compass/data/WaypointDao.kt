package com.moshitech.workmate.feature.compass.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WaypointDao {
    @Query("SELECT * FROM waypoints ORDER BY timestamp DESC")
    fun getAllWaypoints(): Flow<List<WaypointEntity>>
    
    @Query("SELECT * FROM waypoints WHERE id = :id")
    suspend fun getWaypointById(id: Long): WaypointEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoint(waypoint: WaypointEntity): Long
    
    @Update
    suspend fun updateWaypoint(waypoint: WaypointEntity)
    
    @Delete
    suspend fun deleteWaypoint(waypoint: WaypointEntity)
    
    @Query("DELETE FROM waypoints WHERE id = :id")
    suspend fun deleteWaypointById(id: Long)
}
