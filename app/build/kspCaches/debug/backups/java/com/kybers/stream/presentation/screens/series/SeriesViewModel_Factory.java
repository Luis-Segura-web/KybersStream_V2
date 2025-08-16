package com.kybers.stream.presentation.screens.series;

import com.kybers.stream.domain.usecase.xtream.GetSeriesCategoriesUseCase;
import com.kybers.stream.domain.usecase.xtream.GetSeriesStreamsUseCase;
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
public final class SeriesViewModel_Factory implements Factory<SeriesViewModel> {
  private final Provider<GetSeriesCategoriesUseCase> getSeriesCategoriesUseCaseProvider;

  private final Provider<GetSeriesStreamsUseCase> getSeriesStreamsUseCaseProvider;

  private SeriesViewModel_Factory(
      Provider<GetSeriesCategoriesUseCase> getSeriesCategoriesUseCaseProvider,
      Provider<GetSeriesStreamsUseCase> getSeriesStreamsUseCaseProvider) {
    this.getSeriesCategoriesUseCaseProvider = getSeriesCategoriesUseCaseProvider;
    this.getSeriesStreamsUseCaseProvider = getSeriesStreamsUseCaseProvider;
  }

  @Override
  public SeriesViewModel get() {
    return newInstance(getSeriesCategoriesUseCaseProvider.get(), getSeriesStreamsUseCaseProvider.get());
  }

  public static SeriesViewModel_Factory create(
      Provider<GetSeriesCategoriesUseCase> getSeriesCategoriesUseCaseProvider,
      Provider<GetSeriesStreamsUseCase> getSeriesStreamsUseCaseProvider) {
    return new SeriesViewModel_Factory(getSeriesCategoriesUseCaseProvider, getSeriesStreamsUseCaseProvider);
  }

  public static SeriesViewModel newInstance(GetSeriesCategoriesUseCase getSeriesCategoriesUseCase,
      GetSeriesStreamsUseCase getSeriesStreamsUseCase) {
    return new SeriesViewModel(getSeriesCategoriesUseCase, getSeriesStreamsUseCase);
  }
}
