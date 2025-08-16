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
public final class GetVodInfoUseCase_Factory implements Factory<GetVodInfoUseCase> {
  private final Provider<XtreamRepository> xtreamRepositoryProvider;

  private GetVodInfoUseCase_Factory(Provider<XtreamRepository> xtreamRepositoryProvider) {
    this.xtreamRepositoryProvider = xtreamRepositoryProvider;
  }

  @Override
  public GetVodInfoUseCase get() {
    return newInstance(xtreamRepositoryProvider.get());
  }

  public static GetVodInfoUseCase_Factory create(
      Provider<XtreamRepository> xtreamRepositoryProvider) {
    return new GetVodInfoUseCase_Factory(xtreamRepositoryProvider);
  }

  public static GetVodInfoUseCase newInstance(XtreamRepository xtreamRepository) {
    return new GetVodInfoUseCase(xtreamRepository);
  }
}
