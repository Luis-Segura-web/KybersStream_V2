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
public final class IsFavoriteUseCase_Factory implements Factory<IsFavoriteUseCase> {
  private final Provider<FavoriteRepository> favoriteRepositoryProvider;

  private IsFavoriteUseCase_Factory(Provider<FavoriteRepository> favoriteRepositoryProvider) {
    this.favoriteRepositoryProvider = favoriteRepositoryProvider;
  }

  @Override
  public IsFavoriteUseCase get() {
    return newInstance(favoriteRepositoryProvider.get());
  }

  public static IsFavoriteUseCase_Factory create(
      Provider<FavoriteRepository> favoriteRepositoryProvider) {
    return new IsFavoriteUseCase_Factory(favoriteRepositoryProvider);
  }

  public static IsFavoriteUseCase newInstance(FavoriteRepository favoriteRepository) {
    return new IsFavoriteUseCase(favoriteRepository);
  }
}
