package com.kybers.stream.presentation.screens.tv;

import com.kybers.stream.domain.manager.PlaybackManager;
import com.kybers.stream.domain.usecase.preferences.GetUserPreferencesUseCase;
import com.kybers.stream.domain.usecase.preferences.UpdateUserPreferencesUseCase;
import com.kybers.stream.domain.usecase.search.SearchContentUseCase;
import com.kybers.stream.domain.usecase.xtream.GetLiveCategoriesUseCase;
import com.kybers.stream.domain.usecase.xtream.GetLiveStreamsUseCase;
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
public final class TvViewModel_Factory implements Factory<TvViewModel> {
  private final Provider<GetLiveCategoriesUseCase> getLiveCategoriesUseCaseProvider;

  private final Provider<GetLiveStreamsUseCase> getLiveStreamsUseCaseProvider;

  private final Provider<SearchContentUseCase> searchContentUseCaseProvider;

  private final Provider<GetUserPreferencesUseCase> getUserPreferencesUseCaseProvider;

  private final Provider<UpdateUserPreferencesUseCase> updateUserPreferencesUseCaseProvider;

  private final Provider<PlaybackManager> playbackManagerProvider;

  private TvViewModel_Factory(Provider<GetLiveCategoriesUseCase> getLiveCategoriesUseCaseProvider,
      Provider<GetLiveStreamsUseCase> getLiveStreamsUseCaseProvider,
      Provider<SearchContentUseCase> searchContentUseCaseProvider,
      Provider<GetUserPreferencesUseCase> getUserPreferencesUseCaseProvider,
      Provider<UpdateUserPreferencesUseCase> updateUserPreferencesUseCaseProvider,
      Provider<PlaybackManager> playbackManagerProvider) {
    this.getLiveCategoriesUseCaseProvider = getLiveCategoriesUseCaseProvider;
    this.getLiveStreamsUseCaseProvider = getLiveStreamsUseCaseProvider;
    this.searchContentUseCaseProvider = searchContentUseCaseProvider;
    this.getUserPreferencesUseCaseProvider = getUserPreferencesUseCaseProvider;
    this.updateUserPreferencesUseCaseProvider = updateUserPreferencesUseCaseProvider;
    this.playbackManagerProvider = playbackManagerProvider;
  }

  @Override
  public TvViewModel get() {
    return newInstance(getLiveCategoriesUseCaseProvider.get(), getLiveStreamsUseCaseProvider.get(), searchContentUseCaseProvider.get(), getUserPreferencesUseCaseProvider.get(), updateUserPreferencesUseCaseProvider.get(), playbackManagerProvider.get());
  }

  public static TvViewModel_Factory create(
      Provider<GetLiveCategoriesUseCase> getLiveCategoriesUseCaseProvider,
      Provider<GetLiveStreamsUseCase> getLiveStreamsUseCaseProvider,
      Provider<SearchContentUseCase> searchContentUseCaseProvider,
      Provider<GetUserPreferencesUseCase> getUserPreferencesUseCaseProvider,
      Provider<UpdateUserPreferencesUseCase> updateUserPreferencesUseCaseProvider,
      Provider<PlaybackManager> playbackManagerProvider) {
    return new TvViewModel_Factory(getLiveCategoriesUseCaseProvider, getLiveStreamsUseCaseProvider, searchContentUseCaseProvider, getUserPreferencesUseCaseProvider, updateUserPreferencesUseCaseProvider, playbackManagerProvider);
  }

  public static TvViewModel newInstance(GetLiveCategoriesUseCase getLiveCategoriesUseCase,
      GetLiveStreamsUseCase getLiveStreamsUseCase, SearchContentUseCase searchContentUseCase,
      GetUserPreferencesUseCase getUserPreferencesUseCase,
      UpdateUserPreferencesUseCase updateUserPreferencesUseCase, PlaybackManager playbackManager) {
    return new TvViewModel(getLiveCategoriesUseCase, getLiveStreamsUseCase, searchContentUseCase, getUserPreferencesUseCase, updateUserPreferencesUseCase, playbackManager);
  }
}
