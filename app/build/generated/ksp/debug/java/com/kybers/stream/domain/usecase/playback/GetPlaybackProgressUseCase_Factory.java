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
public final class GetPlaybackProgressUseCase_Factory implements Factory<GetPlaybackProgressUseCase> {
  private final Provider<PlaybackProgressRepository> playbackProgressRepositoryProvider;

  private GetPlaybackProgressUseCase_Factory(
      Provider<PlaybackProgressRepository> playbackProgressRepositoryProvider) {
    this.playbackProgressRepositoryProvider = playbackProgressRepositoryProvider;
  }

  @Override
  public GetPlaybackProgressUseCase get() {
    return newInstance(playbackProgressRepositoryProvider.get());
  }

  public static GetPlaybackProgressUseCase_Factory create(
      Provider<PlaybackProgressRepository> playbackProgressRepositoryProvider) {
    return new GetPlaybackProgressUseCase_Factory(playbackProgressRepositoryProvider);
  }

  public static GetPlaybackProgressUseCase newInstance(
      PlaybackProgressRepository playbackProgressRepository) {
    return new GetPlaybackProgressUseCase(playbackProgressRepository);
  }
}
