package com.kybers.stream.domain.usecase.playback;

import com.kybers.stream.domain.repository.PlaybackProgressRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SavePlaybackProgressUseCase_Factory implements Factory<SavePlaybackProgressUseCase> {
  private final Provider<PlaybackProgressRepository> playbackProgressRepositoryProvider;

  private SavePlaybackProgressUseCase_Factory(
      Provider<PlaybackProgressRepository> playbackProgressRepositoryProvider) {
    this.playbackProgressRepositoryProvider = playbackProgressRepositoryProvider;
  }

  @Override
  public SavePlaybackProgressUseCase get() {
    return newInstance(playbackProgressRepositoryProvider.get());
  }

  public static SavePlaybackProgressUseCase_Factory create(
      Provider<PlaybackProgressRepository> playbackProgressRepositoryProvider) {
    return new SavePlaybackProgressUseCase_Factory(playbackProgressRepositoryProvider);
  }

  public static SavePlaybackProgressUseCase newInstance(
      PlaybackProgressRepository playbackProgressRepository) {
    return new SavePlaybackProgressUseCase(playbackProgressRepository);
  }
}
