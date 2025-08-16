package com.kybers.stream.domain.usecase.xtream;

import com.kybers.stream.domain.repository.XtreamRepository;
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
public final class GetSeriesCategoriesUseCase_Factory implements Factory<GetSeriesCategoriesUseCase> {
  private final Provider<XtreamRepository> xtreamRepositoryProvider;

  private GetSeriesCategoriesUseCase_Factory(Provider<XtreamRepository> xtreamRepositoryProvider) {
    this.xtreamRepositoryProvider = xtreamRepositoryProvider;
  }

  @Override
  public GetSeriesCategoriesUseCase get() {
    return newInstance(xtreamRepositoryProvider.get());
  }

  public static GetSeriesCategoriesUseCase_Factory create(
      Provider<XtreamRepository> xtreamRepositoryProvider) {
    return new GetSeriesCategoriesUseCase_Factory(xtreamRepositoryProvider);
  }

  public static GetSeriesCategoriesUseCase newInstance(XtreamRepository xtreamRepository) {
    return new GetSeriesCategoriesUseCase(xtreamRepository);
  }
}
