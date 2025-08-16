package com.kybers.stream.presentation.screens.movies;

import com.kybers.stream.domain.usecase.xtream.GetVodCategoriesUseCase;
import com.kybers.stream.domain.usecase.xtream.GetVodStreamsUseCase;
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
public final class MoviesViewModel_Factory implements Factory<MoviesViewModel> {
  private final Provider<GetVodCategoriesUseCase> getVodCategoriesUseCaseProvider;

  private final Provider<GetVodStreamsUseCase> getVodStreamsUseCaseProvider;

  private MoviesViewModel_Factory(Provider<GetVodCategoriesUseCase> getVodCategoriesUseCaseProvider,
      Provider<GetVodStreamsUseCase> getVodStreamsUseCaseProvider) {
    this.getVodCategoriesUseCaseProvider = getVodCategoriesUseCaseProvider;
    this.getVodStreamsUseCaseProvider = getVodStreamsUseCaseProvider;
  }

  @Override
  public MoviesViewModel get() {
    return newInstance(getVodCategoriesUseCaseProvider.get(), getVodStreamsUseCaseProvider.get());
  }

  public static MoviesViewModel_Factory create(
      Provider<GetVodCategoriesUseCase> getVodCategoriesUseCaseProvider,
      Provider<GetVodStreamsUseCase> getVodStreamsUseCaseProvider) {
    return new MoviesViewModel_Factory(getVodCategoriesUseCaseProvider, getVodStreamsUseCaseProvider);
  }

  public static MoviesViewModel newInstance(GetVodCategoriesUseCase getVodCategoriesUseCase,
      GetVodStreamsUseCase getVodStreamsUseCase) {
    return new MoviesViewModel(getVodCategoriesUseCase, getVodStreamsUseCase);
  }
}
