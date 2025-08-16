package com.kybers.stream.presentation.screens.home;

import com.kybers.stream.domain.usecase.favorites.GetFavoritesUseCase;
import com.kybers.stream.domain.usecase.playback.GetContinueWatchingUseCase;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<GetFavoritesUseCase> getFavoritesUseCaseProvider;

  private final Provider<GetContinueWatchingUseCase> getContinueWatchingUseCaseProvider;

  private HomeViewModel_Factory(Provider<GetFavoritesUseCase> getFavoritesUseCaseProvider,
      Provider<GetContinueWatchingUseCase> getContinueWatchingUseCaseProvider) {
    this.getFavoritesUseCaseProvider = getFavoritesUseCaseProvider;
    this.getContinueWatchingUseCaseProvider = getContinueWatchingUseCaseProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(getFavoritesUseCaseProvider.get(), getContinueWatchingUseCaseProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<GetFavoritesUseCase> getFavoritesUseCaseProvider,
      Provider<GetContinueWatchingUseCase> getContinueWatchingUseCaseProvider) {
    return new HomeViewModel_Factory(getFavoritesUseCaseProvider, getContinueWatchingUseCaseProvider);
  }

  public static HomeViewModel newInstance(GetFavoritesUseCase getFavoritesUseCase,
      GetContinueWatchingUseCase getContinueWatchingUseCase) {
    return new HomeViewModel(getFavoritesUseCase, getContinueWatchingUseCase);
  }
}
