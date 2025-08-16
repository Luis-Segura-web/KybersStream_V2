package com.kybers.stream.di;

import com.kybers.stream.data.local.dao.PlaybackProgressDao;
import com.kybers.stream.data.local.database.KybersStreamDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DatabaseModule_ProvidePlaybackProgressDaoFactory implements Factory<PlaybackProgressDao> {
  private final Provider<KybersStreamDatabase> databaseProvider;

  private DatabaseModule_ProvidePlaybackProgressDaoFactory(
      Provider<KybersStreamDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public PlaybackProgressDao get() {
    return providePlaybackProgressDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvidePlaybackProgressDaoFactory create(
      Provider<KybersStreamDatabase> databaseProvider) {
    return new DatabaseModule_ProvidePlaybackProgressDaoFactory(databaseProvider);
  }

  public static PlaybackProgressDao providePlaybackProgressDao(KybersStreamDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePlaybackProgressDao(database));
  }
}
