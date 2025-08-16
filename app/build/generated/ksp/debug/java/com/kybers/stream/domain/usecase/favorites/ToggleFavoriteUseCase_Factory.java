package com.kybers.stream.domain.usecase.favorites;

import com.kybers.stream.domain.repository.FavoriteRepository;
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
public final class ToggleFavoriteUseCase_Factory implements Factory<ToggleFavoriteUseCase> {
  private final Provider<FavoriteRepository> favoriteRepositoryProvider;

  private ToggleFavoriteUseCase_Factory(Provider<FavoriteRepository> favoriteRepositoryProvider) {
    this.favoriteRepositoryProvider = favoriteRepositoryProvider;
  }

  @Override
  public ToggleFavoriteUseCase get() {
    return newInstance(favoriteRepositoryProvider.get());
  }

  public static ToggleFavoriteUseCase_Factory create(
      Provider<FavoriteRepository> favoriteRepositoryProvider) {
    return new ToggleFavoriteUseCase_Factory(favoriteRepositoryProvider);
  }

  public static ToggleFavoriteUseCase newInstance(FavoriteRepository favoriteRepository) {
    return new ToggleFavoriteUseCase(favoriteRepository);
  }
}
