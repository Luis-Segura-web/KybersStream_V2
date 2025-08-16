package com.kybers.stream.domain.usecase;

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
public final class GetEpgUseCase_Factory implements Factory<GetEpgUseCase> {
  private final Provider<XtreamRepository> xtreamRepositoryProvider;

  private GetEpgUseCase_Factory(Provider<XtreamRepository> xtreamRepositoryProvider) {
    this.xtreamRepositoryProvider = xtreamRepositoryProvider;
  }

  @Override
  public GetEpgUseCase get() {
    return newInstance(xtreamRepositoryProvider.get());
  }

  public static GetEpgUseCase_Factory create(Provider<XtreamRepository> xtreamRepositoryProvider) {
    return new GetEpgUseCase_Factory(xtreamRepositoryProvider);
  }

  public static GetEpgUseCase newInstance(XtreamRepository xtreamRepository) {
    return new GetEpgUseCase(xtreamRepository);
  }
}
