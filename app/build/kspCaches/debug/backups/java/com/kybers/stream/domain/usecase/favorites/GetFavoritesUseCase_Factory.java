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
public final class GetFavoritesUseCase_Factory implements Factory<GetFavoritesUseCase> {
  private final Provider<FavoriteRepository> favoriteRepositoryProvider;

  private GetFavoritesUseCase_Factory(Provider<FavoriteRepository> favoriteRepositoryProvider) {
    this.favoriteRepositoryProvider = favoriteRepositoryProvider;
  }

  @Override
  public GetFavoritesUseCase get() {
    return newInstance(favoriteRepositoryProvider.get());
  }

  public static GetFavoritesUseCase_Factory create(
      Provider<FavoriteRepository> favoriteRepositoryProvider) {
    return new GetFavoritesUseCase_Factory(favoriteRepositoryProvider);
  }

  public static GetFavoritesUseCase newInstance(FavoriteRepository favoriteRepository) {
    return new GetFavoritesUseCase(favoriteRepository);
  }
}
