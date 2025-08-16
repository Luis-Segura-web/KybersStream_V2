package com.kybers.stream.presentation.screens.splash;

import com.kybers.stream.domain.usecase.IsUserLoggedInUseCase;
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
public final class SplashViewModel_Factory implements Factory<SplashViewModel> {
  private final Provider<IsUserLoggedInUseCase> isUserLoggedInUseCaseProvider;

  private SplashViewModel_Factory(Provider<IsUserLoggedInUseCase> isUserLoggedInUseCaseProvider) {
    this.isUserLoggedInUseCaseProvider = isUserLoggedInUseCaseProvider;
  }

  @Override
  public SplashViewModel get() {
    return newInstance(isUserLoggedInUseCaseProvider.get());
  }

  public static SplashViewModel_Factory create(
      Provider<IsUserLoggedInUseCase> isUserLoggedInUseCaseProvider) {
    return new SplashViewModel_Factory(isUserLoggedInUseCaseProvider);
  }

  public static SplashViewModel newInstance(IsUserLoggedInUseCase isUserLoggedInUseCase) {
    return new SplashViewModel(isUserLoggedInUseCase);
  }
}
