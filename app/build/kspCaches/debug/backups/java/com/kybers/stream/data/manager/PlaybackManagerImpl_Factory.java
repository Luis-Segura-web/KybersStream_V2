package com.kybers.stream.data.manager;

import android.content.Context;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.ExoPlayer;
import com.kybers.stream.domain.usecase.playback.SavePlaybackProgressUseCase;
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
public final class PlaybackManagerImpl_Factory implements Factory<PlaybackManagerImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<ExoPlayer> exoPlayerInstanceProvider;

  private final Provider<DataSource.Factory> dataSourceFactoryProvider;

  private final Provider<SavePlaybackProgressUseCase> savePlaybackProgressUseCaseProvider;

  private PlaybackManagerImpl_Factory(Provider<Context> contextProvider,
      Provider<ExoPlayer> exoPlayerInstanceProvider,
      Provider<DataSource.Factory> dataSourceFactoryProvider,
      Provider<SavePlaybackProgressUseCase> savePlaybackProgressUseCaseProvider) {
    this.contextProvider = contextProvider;
    this.exoPlayerInstanceProvider = exoPlayerInstanceProvider;
    this.dataSourceFactoryProvider = dataSourceFactoryProvider;
    this.savePlaybackProgressUseCaseProvider = savePlaybackProgressUseCaseProvider;
  }

  @Override
  public PlaybackManagerImpl get() {
    return newInstance(contextProvider.get(), exoPlayerInstanceProvider.get(), dataSourceFactoryProvider.get(), savePlaybackProgressUseCaseProvider.get());
  }

  public static PlaybackManagerImpl_Factory create(Provider<Context> contextProvider,
      Provider<ExoPlayer> exoPlayerInstanceProvider,
      Provider<DataSource.Factory> dataSourceFactoryProvider,
      Provider<SavePlaybackProgressUseCase> savePlaybackProgressUseCaseProvider) {
    return new PlaybackManagerImpl_Factory(contextProvider, exoPlayerInstanceProvider, dataSourceFactoryProvider, savePlaybackProgressUseCaseProvider);
  }

  public static PlaybackManagerImpl newInstance(Context context, ExoPlayer exoPlayerInstance,
      DataSource.Factory dataSourceFactory,
      SavePlaybackProgressUseCase savePlaybackProgressUseCase) {
    return new PlaybackManagerImpl(context, exoPlayerInstance, dataSourceFactory, savePlaybackProgressUseCase);
  }
}
