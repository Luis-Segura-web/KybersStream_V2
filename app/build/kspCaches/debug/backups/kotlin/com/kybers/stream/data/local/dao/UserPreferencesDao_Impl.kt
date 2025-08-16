package com.kybers.stream.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.kybers.stream.`data`.local.entity.UserPreferencesEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class UserPreferencesDao_Impl(
  __db: RoomDatabase,
) : UserPreferencesDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfUserPreferencesEntity: EntityInsertAdapter<UserPreferencesEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfUserPreferencesEntity = object :
        EntityInsertAdapter<UserPreferencesEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `user_preferences` (`key`,`value`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UserPreferencesEntity) {
        statement.bindText(1, entity.key)
        statement.bindText(2, entity.value)
      }
    }
  }

  public override suspend fun setPreference(preference: UserPreferencesEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfUserPreferencesEntity.insert(_connection, preference)
  }

  public override suspend fun setPreferences(preferences: Map<String, String>): Unit =
      performInTransactionSuspending(__db) {
    super@UserPreferencesDao_Impl.setPreferences(preferences)
  }

  public override fun getAllPreferences(): Flow<List<UserPreferencesEntity>> {
    val _sql: String = "SELECT * FROM user_preferences"
    return createFlow(__db, false, arrayOf("user_preferences")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfKey: Int = getColumnIndexOrThrow(_stmt, "key")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _result: MutableList<UserPreferencesEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: UserPreferencesEntity
          val _tmpKey: String
          _tmpKey = _stmt.getText(_columnIndexOfKey)
          val _tmpValue: String
          _tmpValue = _stmt.getText(_columnIndexOfValue)
          _item = UserPreferencesEntity(_tmpKey,_tmpValue)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPreference(key: String): String? {
    val _sql: String = "SELECT value FROM user_preferences WHERE key = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, key)
        val _result: String?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getText(0)
          }
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPreferenceFlow(key: String): Flow<String?> {
    val _sql: String = "SELECT value FROM user_preferences WHERE key = ?"
    return createFlow(__db, false, arrayOf("user_preferences")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, key)
        val _result: String?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getText(0)
          }
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun removePreference(key: String) {
    val _sql: String = "DELETE FROM user_preferences WHERE key = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, key)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAllPreferences() {
    val _sql: String = "DELETE FROM user_preferences"
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
