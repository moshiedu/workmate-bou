package com.moshitech.workmate.feature.scanner.data.local.dao

import androidx.room.*
import com.moshitech.workmate.feature.scanner.data.local.entity.ScannedDocumentEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for scanned documents
 */
@Dao
interface ScannedDocumentDao {
    
    @Query("SELECT * FROM scanned_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<ScannedDocumentEntity>>
    
    @Query("SELECT * FROM scanned_documents WHERE id = :id")
    suspend fun getDocumentById(id: String): ScannedDocumentEntity?
    
    @Query("SELECT * FROM scanned_documents WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchDocuments(query: String): Flow<List<ScannedDocumentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: ScannedDocumentEntity)
    
    @Delete
    suspend fun deleteDocument(document: ScannedDocumentEntity)
    
    @Query("DELETE FROM scanned_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)
    
    @Query("DELETE FROM scanned_documents")
    suspend fun deleteAllDocuments()
}
