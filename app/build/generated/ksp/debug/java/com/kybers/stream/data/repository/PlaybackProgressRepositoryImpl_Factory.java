package com.kybers.stream.data.repository;

import com.kybers.stream.data.local.dao.PlaybackProgressDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class PlaybackProgressRepositoryImpl_Factory implements Factory<PlaybackProgressRepositoryImpl> {
  private final Provider<PlaybackProgressDao> playbackProgressDaoProvider;

  private PlaybackProgressRepositoryImpl_Factory(
      Provider<PlaybackProgressDao> playbackProgressDaoProvider) {
    this.playbackProgressDaoProvider = playbackProgressDaoProvider;
  }

  @Override
  public PlaybackProgressRepositoryImpl get() {
    return newInstance(playbackProgressDaoProvider.get());
  }

  public static PlaybackProgressRepositoryImpl_Factory create(
      Provider<PlaybackProgressDao> playbackProgressDaoProvider) {
    return new PlaybackProgressRepositoryImpl_Factory(playbackProgressDaoProvider);
  }

  public static PlaybackProgressRepositoryImpl newInstance(
      PlaybackProgressDao playbackProgressDao) {
    return new PlaybackProgressRepositoryImpl(playbackProgressDao);
  }
}
