package com.kybers.stream.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.getTotalChangedRows
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.kybers.stream.`data`.local.database.Converters
import com.kybers.stream.`data`.local.entity.FavoriteEntity
import com.kybers.stream.domain.model.ContentType
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class FavoriteDao_Impl(
  __db: RoomDatabase,
) : FavoriteDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFavoriteEntity: EntityInsertAdapter<FavoriteEntity>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__insertAdapterOfFavoriteEntity = object : EntityInsertAdapter<FavoriteEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `favorites` (`id`,`contentId`,`contentType`,`name`,`imageUrl`,`categoryId`,`addedTimestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FavoriteEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.contentId)
        val _tmp: String = __converters.fromContentType(entity.contentType)
        statement.bindText(3, _tmp)
        statement.bindText(4, entity.name)
        val _tmpImageUrl: String? = entity.imageUrl
        if (_tmpImageUrl == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpImageUrl)
        }
        val _tmpCategoryId: String? = entity.categoryId
        if (_tmpCategoryId == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpCategoryId)
        }
        statement.bindLong(7, entity.addedTimestamp)
      }
    }
  }

  public override suspend fun insertFavorite(favorite: FavoriteEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfFavoriteEntity.insertAndReturnId(_connection, favorite)
    _result
  }

  public override fun getAllFavorites(): Flow<List<FavoriteEntity>> {
    val _sql: String = "SELECT * FROM favorites ORDER BY addedTimestamp DESC"
    return createFlow(__db, false, arrayOf("favorites")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfContentId: Int = getColumnIndexOrThrow(_stmt, "contentId")
        val _columnIndexOfContentType: Int = getColumnIndexOrThrow(_stmt, "contentType")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfAddedTimestamp: Int = getColumnIndexOrThrow(_stmt, "addedTimestamp")
        val _result: MutableList<FavoriteEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FavoriteEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpContentId: String
          _tmpContentId = _stmt.getText(_columnIndexOfContentId)
          val _tmpContentType: ContentType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfContentType)
          _tmpContentType = __converters.toContentType(_tmp)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpCategoryId: String?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getText(_columnIndexOfCategoryId)
          }
          val _tmpAddedTimestamp: Long
          _tmpAddedTimestamp = _stmt.getLong(_columnIndexOfAddedTimestamp)
          _item =
              FavoriteEntity(_tmpId,_tmpContentId,_tmpContentType,_tmpName,_tmpImageUrl,_tmpCategoryId,_tmpAddedTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getFavoritesByType(contentType: ContentType): Flow<List<FavoriteEntity>> {
    val _sql: String = "SELECT * FROM favorites WHERE contentType = ? ORDER BY addedTimestamp DESC"
    return createFlow(__db, false, arrayOf("favorites")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: String = __converters.fromContentType(contentType)
        _stmt.bindText(_argIndex, _tmp)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfContentId: Int = getColumnIndexOrThrow(_stmt, "contentId")
        val _columnIndexOfContentType: Int = getColumnIndexOrThrow(_stmt, "contentType")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfAddedTimestamp: Int = getColumnIndexOrThrow(_stmt, "addedTimestamp")
        val _result: MutableList<FavoriteEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FavoriteEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpContentId: String
          _tmpContentId = _stmt.getText(_columnIndexOfContentId)
          val _tmpContentType: ContentType
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfContentType)
          _tmpContentType = __converters.toContentType(_tmp_1)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpCategoryId: String?
          if (_stmt.isNull(_columnIndexOfCategoryId)) {
            _tmpCategoryId = null
          } else {
            _tmpCategoryId = _stmt.getText(_columnIndexOfCategoryId)
          }
          val _tmpAddedTimestamp: Long
          _tmpAddedTimestamp = _stmt.getLong(_columnIndexOfAddedTimestamp)
          _item =
              FavoriteEntity(_tmpId,_tmpContentId,_tmpContentType,_tmpName,_tmpImageUrl,_tmpCategoryId,_tmpAddedTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun isFavorite(contentId: String, contentType: ContentType): Boolean {
    val _sql: String =
        "SELECT EXISTS(SELECT 1 FROM favorites WHERE contentId = ? AND contentType = ?)"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, contentId)
        _argIndex = 2
        val _tmp: String = __converters.fromContentType(contentType)
        _stmt.bindText(_argIndex, _tmp)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(0).toInt()
          _result = _tmp_1 != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun isFavoriteFlow(contentId: String, contentType: ContentType): Flow<Boolean> {
    val _sql: String =
        "SELECT EXISTS(SELECT 1 FROM favorites WHERE contentId = ? AND contentType = ?)"
    return createFlow(__db, false, arrayOf("favorites")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, contentId)
        _argIndex = 2
        val _tmp: String = __converters.fromContentType(contentType)
        _stmt.bindText(_argIndex, _tmp)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(0).toInt()
          _result = _tmp_1 != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getFavoritesCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM favorites"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun removeFavorite(contentId: String, contentType: ContentType): Int {
    val _sql: String = "DELETE FROM favorites WHERE contentId = ? AND contentType = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, contentId)
        _argIndex = 2
        val _tmp: String = __converters.fromContentType(contentType)
        _stmt.bindText(_argIndex, _tmp)
        _stmt.step()
        getTotalChangedRows(_connection)
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAllFavorites() {
    val _sql: String = "DELETE FROM favorites"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
