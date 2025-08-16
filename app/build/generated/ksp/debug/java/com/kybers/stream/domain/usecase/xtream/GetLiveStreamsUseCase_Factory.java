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
public final class GetLiveStreamsUseCase_Factory implements Factory<GetLiveStreamsUseCase> {
  private final Provider<XtreamRepository> xtreamRepositoryProvider;

  private GetLiveStreamsUseCase_Factory(Provider<XtreamRepository> xtreamRepositoryProvider) {
    this.xtreamRepositoryProvider = xtreamRepositoryProvider;
  }

  @Override
  public GetLiveStreamsUseCase get() {
    return newInstance(xtreamRepositoryProvider.get());
  }

  public static GetLiveStreamsUseCase_Factory create(
      Provider<XtreamRepository> xtreamRepositoryProvider) {
    return new GetLiveStreamsUseCase_Factory(xtreamRepositoryProvider);
  }

  public static GetLiveStreamsUseCase newInstance(XtreamRepository xtreamRepository) {
    return new GetLiveStreamsUseCase(xtreamRepository);
  }
}
