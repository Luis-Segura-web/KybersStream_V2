package com.kybers.stream.domain.usecase.search;

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
public final class SearchContentUseCase_Factory implements Factory<SearchContentUseCase> {
  private final Provider<XtreamRepository> xtreamRepositoryProvider;

  private SearchContentUseCase_Factory(Provider<XtreamRepository> xtreamRepositoryProvider) {
    this.xtreamRepositoryProvider = xtreamRepositoryProvider;
  }

  @Override
  public SearchContentUseCase get() {
    return newInstance(xtreamRepositoryProvider.get());
  }

  public static SearchContentUseCase_Factory create(
      Provider<XtreamRepository> xtreamRepositoryProvider) {
    return new SearchContentUseCase_Factory(xtreamRepositoryProvider);
  }

  public static SearchContentUseCase newInstance(XtreamRepository xtreamRepository) {
    return new SearchContentUseCase(xtreamRepository);
  }
}
