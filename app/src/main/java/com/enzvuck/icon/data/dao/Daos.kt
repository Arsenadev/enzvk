package com.enzvuck.icon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.enzvuck.icon.data.entity.CustomIconEntity
import com.enzvuck.icon.data.entity.IconPackEntity
import com.enzvuck.icon.data.entity.IconPackItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomIconDao {
    @Query("SELECT * FROM custom_icons ORDER BY updatedAt DESC")
    fun getAllCustomIcons(): Flow<List<CustomIconEntity>>

    @Query("SELECT * FROM custom_icons WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): CustomIconEntity?

    @Query("SELECT * FROM custom_icons WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CustomIconEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(icon: CustomIconEntity): Long

    @Update
    suspend fun update(icon: CustomIconEntity)

    @Query("DELETE FROM custom_icons WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM custom_icons WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    @Query("DELETE FROM custom_icons")
    suspend fun deleteAll()
}

@Dao
interface IconPackDao {
    @Query("SELECT * FROM icon_packs ORDER BY updatedAt DESC")
    fun getAllPacks(): Flow<List<IconPackEntity>>

    @Query("SELECT * FROM icon_packs WHERE id = :id LIMIT 1")
    suspend fun getPackById(id: Long): IconPackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPack(pack: IconPackEntity): Long

    @Update
    suspend fun updatePack(pack: IconPackEntity)

    @Query("DELETE FROM icon_packs WHERE id = :packId")
    suspend fun deletePackById(packId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackItems(items: List<IconPackItemEntity>)

    @Query("SELECT * FROM icon_pack_items WHERE packId = :packId")
    fun getItemsForPack(packId: Long): Flow<List<IconPackItemEntity>>

    @Query("SELECT * FROM icon_pack_items WHERE packId = :packId")
    suspend fun getItemsForPackSync(packId: Long): List<IconPackItemEntity>

    @Query("DELETE FROM icon_pack_items WHERE packId = :packId")
    suspend fun deleteItemsForPack(packId: Long)
}
