package com.kybers.stream.`data`.local.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.kybers.stream.`data`.local.dao.FavoriteDao
import com.kybers.stream.`data`.local.dao.FavoriteDao_Impl
import com.kybers.stream.`data`.local.dao.PlaybackProgressDao
import com.kybers.stream.`data`.local.dao.PlaybackProgressDao_Impl
import com.kybers.stream.`data`.local.dao.UserPreferencesDao
import com.kybers.stream.`data`.local.dao.UserPreferencesDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class KybersStreamDatabase_Impl : KybersStreamDatabase() {
  private val _favoriteDao: Lazy<FavoriteDao> = lazy {
    FavoriteDao_Impl(this)
  }

  private val _playbackProgressDao: Lazy<PlaybackProgressDao> = lazy {
    PlaybackProgressDao_Impl(this)
  }

  private val _userPreferencesDao: Lazy<UserPreferencesDao> = lazy {
    UserPreferencesDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "08393d1d02b74148d611244b3fd27b6c", "a442970f03ee3fbba69388e9fb9dee71") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `favorites` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `contentId` TEXT NOT NULL, `contentType` TEXT NOT NULL, `name` TEXT NOT NULL, `imageUrl` TEXT, `categoryId` TEXT, `addedTimestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_favorites_contentId_contentType` ON `favorites` (`contentId`, `contentType`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `playback_progress` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `contentId` TEXT NOT NULL, `contentType` TEXT NOT NULL, `positionMs` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `lastUpdated` INTEGER NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_playback_progress_contentId_contentType` ON `playback_progress` (`contentId`, `contentType`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `user_preferences` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '08393d1d02b74148d611244b3fd27b6c')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `favorites`")
        connection.execSQL("DROP TABLE IF EXISTS `playback_progress`")
        connection.execSQL("DROP TABLE IF EXISTS `user_preferences`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsFavorites: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFavorites.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFavorites.put("contentId", TableInfo.Column("contentId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFavorites.put("contentType", TableInfo.Column("contentType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFavorites.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFavorites.put("imageUrl", TableInfo.Column("imageUrl", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFavorites.put("categoryId", TableInfo.Column("categoryId", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFavorites.put("addedTimestamp", TableInfo.Column("addedTimestamp", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFavorites: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFavorites: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesFavorites.add(TableInfo.Index("index_favorites_contentId_contentType", true,
            listOf("contentId", "contentType"), listOf("ASC", "ASC")))
        val _infoFavorites: TableInfo = TableInfo("favorites", _columnsFavorites,
            _foreignKeysFavorites, _indicesFavorites)
        val _existingFavorites: TableInfo = read(connection, "favorites")
        if (!_infoFavorites.equals(_existingFavorites)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |favorites(com.kybers.stream.data.local.entity.FavoriteEntity).
              | Expected:
              |""".trimMargin() + _infoFavorites + """
              |
              | Found:
              |""".trimMargin() + _existingFavorites)
        }
        val _columnsPlaybackProgress: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPlaybackProgress.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackProgress.put("contentId", TableInfo.Column("contentId", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackProgress.put("contentType", TableInfo.Column("contentType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackProgress.put("positionMs", TableInfo.Column("positionMs", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackProgress.put("durationMs", TableInfo.Column("durationMs", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackProgress.put("lastUpdated", TableInfo.Column("lastUpdated", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPlaybackProgress: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPlaybackProgress: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesPlaybackProgress.add(TableInfo.Index("index_playback_progress_contentId_contentType",
            true, listOf("contentId", "contentType"), listOf("ASC", "ASC")))
        val _infoPlaybackProgress: TableInfo = TableInfo("playback_progress",
            _columnsPlaybackProgress, _foreignKeysPlaybackProgress, _indicesPlaybackProgress)
        val _existingPlaybackProgress: TableInfo = read(connection, "playback_progress")
        if (!_infoPlaybackProgress.equals(_existingPlaybackProgress)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |playback_progress(com.kybers.stream.data.local.entity.PlaybackProgressEntity).
              | Expected:
              |""".trimMargin() + _infoPlaybackProgress + """
              |
              | Found:
              |""".trimMargin() + _existingPlaybackProgress)
        }
        val _columnsUserPreferences: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUserPreferences.put("key", TableInfo.Column("key", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserPreferences.put("value", TableInfo.Column("value", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUserPreferences: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUserPreferences: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoUserPreferences: TableInfo = TableInfo("user_preferences", _columnsUserPreferences,
            _foreignKeysUserPreferences, _indicesUserPreferences)
        val _existingUserPreferences: TableInfo = read(connection, "user_preferences")
        if (!_infoUserPreferences.equals(_existingUserPreferences)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |user_preferences(com.kybers.stream.data.local.entity.UserPreferencesEntity).
              | Expected:
              |""".trimMargin() + _infoUserPreferences + """
              |
              | Found:
              |""".trimMargin() + _existingUserPreferences)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "favorites",
        "playback_progress", "user_preferences")
  }

  public override fun clearAllTables() {
    super.performClear(false, "favorites", "playback_progress", "user_preferences")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(FavoriteDao::class, FavoriteDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PlaybackProgressDao::class,
        PlaybackProgressDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(UserPreferencesDao::class,
        UserPreferencesDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun favoriteDao(): FavoriteDao = _favoriteDao.value

  public override fun playbackProgressDao(): PlaybackProgressDao = _playbackProgressDao.value

  public override fun userPreferencesDao(): UserPreferencesDao = _userPreferencesDao.value
}
