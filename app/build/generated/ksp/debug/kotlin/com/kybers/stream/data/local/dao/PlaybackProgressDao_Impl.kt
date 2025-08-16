package com.kybers.stream.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.getTotalChangedRows
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.kybers.stream.`data`.local.database.Converters
import com.kybers.stream.`data`.local.entity.PlaybackProgressEntity
import com.kybers.stream.domain.model.ContentType
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PlaybackProgressDao_Impl(
  __db: RoomDatabase,
) : PlaybackProgressDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPlaybackProgressEntity: EntityInsertAdapter<PlaybackProgressEntity>

  private val __converters: Converters = Converters()

  private val __updateAdapterOfPlaybackProgressEntity:
      EntityDeleteOrUpdateAdapter<PlaybackProgressEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPlaybackProgressEntity = object :
        EntityInsertAdapter<PlaybackProgressEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `playback_progress` (`id`,`contentId`,`contentType`,`positionMs`,`durationMs`,`lastUpdated`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlaybackProgressEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.contentId)
        val _tmp: String = __converters.fromContentType(entity.contentType)
        statement.bindText(3, _tmp)
        statement.bindLong(4, entity.positionMs)
        statement.bindLong(5, entity.durationMs)
        statement.bindLong(6, entity.lastUpdated)
      }
    }
    this.__updateAdapterOfPlaybackProgressEntity = object :
        EntityDeleteOrUpdateAdapter<PlaybackProgressEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `playback_progress` SET `id` = ?,`contentId` = ?,`contentType` = ?,`positionMs` = ?,`durationMs` = ?,`lastUpdated` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PlaybackProgressEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.contentId)
        val _tmp: String = __converters.fromContentType(entity.contentType)
        statement.bindText(3, _tmp)
        statement.bindLong(4, entity.positionMs)
        statement.bindLong(5, entity.durationMs)
        statement.bindLong(6, entity.lastUpdated)
        statement.bindLong(7, entity.id)
      }
    }
  }

  public override suspend fun insertProgress(progress: PlaybackProgressEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfPlaybackProgressEntity.insertAndReturnId(_connection,
        progress)
    _result
  }

  public override suspend fun updateProgress(progress: PlaybackProgressEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfPlaybackProgressEntity.handle(_connection, progress)
  }

  public override fun getAllProgress(): Flow<List<PlaybackProgressEntity>> {
    val _sql: String = "SELECT * FROM playback_progress ORDER BY lastUpdated DESC"
    return createFlow(__db, false, arrayOf("playback_progress")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfContentId: Int = getColumnIndexOrThrow(_stmt, "contentId")
        val _columnIndexOfContentType: Int = getColumnIndexOrThrow(_stmt, "contentType")
        val _columnIndexOfPositionMs: Int = getColumnIndexOrThrow(_stmt, "positionMs")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfLastUpdated: Int = getColumnIndexOrThrow(_stmt, "lastUpdated")
        val _result: MutableList<PlaybackProgressEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlaybackProgressEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpContentId: String
          _tmpContentId = _stmt.getText(_columnIndexOfContentId)
          val _tmpContentType: ContentType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfContentType)
          _tmpContentType = __converters.toContentType(_tmp)
          val _tmpPositionMs: Long
          _tmpPositionMs = _stmt.getLong(_columnIndexOfPositionMs)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpLastUpdated: Long
          _tmpLastUpdated = _stmt.getLong(_columnIndexOfLastUpdated)
          _item =
              PlaybackProgressEntity(_tmpId,_tmpContentId,_tmpContentType,_tmpPositionMs,_tmpDurationMs,_tmpLastUpdated)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getContinueWatchingItems(contentTypes: List<ContentType>, limit: Int):
      Flow<List<PlaybackProgressEntity>> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM playback_progress WHERE contentType IN (")
    val _inputSize: Int = contentTypes.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(") ORDER BY lastUpdated DESC LIMIT ")
    _stringBuilder.append("?")
    val _sql: String = _stringBuilder.toString()
    return createFlow(__db, false, arrayOf("playback_progress")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: ContentType in contentTypes) {
          val _tmp: String = __converters.fromContentType(_item)
          _stmt.bindText(_argIndex, _tmp)
          _argIndex++
        }
        _argIndex = 1 + _inputSize
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfContentId: Int = getColumnIndexOrThrow(_stmt, "contentId")
        val _columnIndexOfContentType: Int = getColumnIndexOrThrow(_stmt, "contentType")
        val _columnIndexOfPositionMs: Int = getColumnIndexOrThrow(_stmt, "positionMs")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfLastUpdated: Int = getColumnIndexOrThrow(_stmt, "lastUpdated")
        val _result: MutableList<PlaybackProgressEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: PlaybackProgressEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpContentId: String
          _tmpContentId = _stmt.getText(_columnIndexOfContentId)
          val _tmpContentType: ContentType
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfContentType)
          _tmpContentType = __converters.toContentType(_tmp_1)
          val _tmpPositionMs: Long
          _tmpPositionMs = _stmt.getLong(_columnIndexOfPositionMs)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpLastUpdated: Long
          _tmpLastUpdated = _stmt.getLong(_columnIndexOfLastUpdated)
          _item_1 =
              PlaybackProgressEntity(_tmpId,_tmpContentId,_tmpContentType,_tmpPositionMs,_tmpDurationMs,_tmpLastUpdated)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getProgress(contentId: String, contentType: ContentType):
      PlaybackProgressEntity? {
    val _sql: String = "SELECT * FROM playback_progress WHERE contentId = ? AND contentType = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, contentId)
        _argIndex = 2
        val _tmp: String = __converters.fromContentType(contentType)
        _stmt.bindText(_argIndex, _tmp)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfContentId: Int = getColumnIndexOrThrow(_stmt, "contentId")
        val _columnIndexOfContentType: Int = getColumnIndexOrThrow(_stmt, "contentType")
        val _columnIndexOfPositionMs: Int = getColumnIndexOrThrow(_stmt, "positionMs")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfLastUpdated: Int = getColumnIndexOrThrow(_stmt, "lastUpdated")
        val _result: PlaybackProgressEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpContentId: String
          _tmpContentId = _stmt.getText(_columnIndexOfContentId)
          val _tmpContentType: ContentType
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfContentType)
          _tmpContentType = __converters.toContentType(_tmp_1)
          val _tmpPositionMs: Long
          _tmpPositionMs = _stmt.getLong(_columnIndexOfPositionMs)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpLastUpdated: Long
          _tmpLastUpdated = _stmt.getLong(_columnIndexOfLastUpdated)
          _result =
              PlaybackProgressEntity(_tmpId,_tmpContentId,_tmpContentType,_tmpPositionMs,_tmpDurationMs,_tmpLastUpdated)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getProgressFlow(contentId: String, contentType: ContentType):
      Flow<PlaybackProgressEntity?> {
    val _sql: String = "SELECT * FROM playback_progress WHERE contentId = ? AND contentType = ?"
    return createFlow(__db, false, arrayOf("playback_progress")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, contentId)
        _argIndex = 2
        val _tmp: String = __converters.fromContentType(contentType)
        _stmt.bindText(_argIndex, _tmp)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfContentId: Int = getColumnIndexOrThrow(_stmt, "contentId")
        val _columnIndexOfContentType: Int = getColumnIndexOrThrow(_stmt, "contentType")
        val _columnIndexOfPositionMs: Int = getColumnIndexOrThrow(_stmt, "positionMs")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfLastUpdated: Int = getColumnIndexOrThrow(_stmt, "lastUpdated")
        val _result: PlaybackProgressEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpContentId: String
          _tmpContentId = _stmt.getText(_columnIndexOfContentId)
          val _tmpContentType: ContentType
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfContentType)
          _tmpContentType = __converters.toContentType(_tmp_1)
          val _tmpPositionMs: Long
          _tmpPositionMs = _stmt.getLong(_columnIndexOfPositionMs)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpLastUpdated: Long
          _tmpLastUpdated = _stmt.getLong(_columnIndexOfLastUpdated)
          _result =
              PlaybackProgressEntity(_tmpId,_tmpContentId,_tmpContentType,_tmpPositionMs,_tmpDurationMs,_tmpLastUpdated)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun removeProgress(contentId: String, contentType: ContentType): Int {
    val _sql: String = "DELETE FROM playback_progress WHERE contentId = ? AND contentType = ?"
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

  public override suspend fun removeCompletedProgress() {
    val _sql: String =
        "DELETE FROM playback_progress WHERE positionMs / CAST(durationMs AS REAL) > 0.95"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun removeOldProgress(cutoffTime: Long) {
    val _sql: String = "DELETE FROM playback_progress WHERE lastUpdated < ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, cutoffTime)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAllProgress() {
    val _sql: String = "DELETE FROM playback_progress"
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
