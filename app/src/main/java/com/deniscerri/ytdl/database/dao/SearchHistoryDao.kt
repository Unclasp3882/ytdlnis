package com.deniscerri.ytdl.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deniscerri.ytdl.database.models.SearchHistoryItem

@Dao
interface SearchHistoryDao {
    @Query("SELECT * from searchHistory ORDER BY id DESC LIMIT 25")
    fun getAll() : List<SearchHistoryItem>

    @Query("SELECT * from searchHistory WHERE `query` COLLATE NOCASE ='%'||:keyword||'%'")
    fun getAllByKeyword(keyword: String) : List<SearchHistoryItem>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(newItem: SearchHistoryItem)

    @Query("DELETE FROM searchHistory")
    suspend fun deleteAll()

    @Query("DELETE FROM searchHistory WHERE `query`=:query")
    suspend fun delete(query: String)
}