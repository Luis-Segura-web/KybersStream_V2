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
public final class GetVodStreamsUseCase_Factory implements Factory<GetVodStreamsUseCase> {
  private final Provider<XtreamRepository> xtreamRepositoryProvider;

  private GetVodStreamsUseCase_Factory(Provider<XtreamRepository> xtreamRepositoryProvider) {
    this.xtreamRepositoryProvider = xtreamRepositoryProvider;
  }

  @Override
  public GetVodStreamsUseCase get() {
    return newInstance(xtreamRepositoryProvider.get());
  }

  public static GetVodStreamsUseCase_Factory create(
      Provider<XtreamRepository> xtreamRepositoryProvider) {
    return new GetVodStreamsUseCase_Factory(xtreamRepositoryProvider);
  }

  public static GetVodStreamsUseCase newInstance(XtreamRepository xtreamRepository) {
    return new GetVodStreamsUseCase(xtreamRepository);
  }
}
