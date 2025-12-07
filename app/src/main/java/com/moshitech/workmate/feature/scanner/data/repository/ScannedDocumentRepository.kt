package com.moshitech.workmate.feature.scanner.data.repository

import com.moshitech.workmate.feature.scanner.data.local.dao.ScannedDocumentDao
import com.moshitech.workmate.feature.scanner.data.mapper.toDomain
import com.moshitech.workmate.feature.scanner.data.mapper.toEntity
import com.moshitech.workmate.feature.scanner.domain.model.ScannedDocument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for scanned documents
 */
@Singleton
class ScannedDocumentRepository @Inject constructor(
    private val dao: ScannedDocumentDao
) {
    
    fun getAllDocuments(): Flow<List<ScannedDocument>> {
        return dao.getAllDocuments().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getDocumentById(id: String): ScannedDocument? {
        return dao.getDocumentById(id)?.toDomain()
    }
    
    fun searchDocuments(query: String): Flow<List<ScannedDocument>> {
        return dao.searchDocuments(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun insertDocument(document: ScannedDocument) {
        dao.insertDocument(document.toEntity())
    }
    
    suspend fun deleteDocument(document: ScannedDocument) {
        dao.deleteDocument(document.toEntity())
    }
    
    suspend fun deleteDocumentById(id: String) {
        dao.deleteDocumentById(id)
    }
    
    suspend fun deleteAllDocuments() {
        dao.deleteAllDocuments()
    }
}
