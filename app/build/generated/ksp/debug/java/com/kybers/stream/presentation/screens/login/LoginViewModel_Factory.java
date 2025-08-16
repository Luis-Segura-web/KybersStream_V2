package com.kybers.stream.presentation.screens.login;

import com.kybers.stream.domain.usecase.GetSavedProfilesUseCase;
import com.kybers.stream.domain.usecase.LoginUserUseCase;
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
public final class LoginViewModel_Factory implements Factory<LoginViewModel> {
  private final Provider<LoginUserUseCase> loginUserUseCaseProvider;

  private final Provider<GetSavedProfilesUseCase> getSavedProfilesUseCaseProvider;

  private LoginViewModel_Factory(Provider<LoginUserUseCase> loginUserUseCaseProvider,
      Provider<GetSavedProfilesUseCase> getSavedProfilesUseCaseProvider) {
    this.loginUserUseCaseProvider = loginUserUseCaseProvider;
    this.getSavedProfilesUseCaseProvider = getSavedProfilesUseCaseProvider;
  }

  @Override
  public LoginViewModel get() {
    return newInstance(loginUserUseCaseProvider.get(), getSavedProfilesUseCaseProvider.get());
  }

  public static LoginViewModel_Factory create(Provider<LoginUserUseCase> loginUserUseCaseProvider,
      Provider<GetSavedProfilesUseCase> getSavedProfilesUseCaseProvider) {
    return new LoginViewModel_Factory(loginUserUseCaseProvider, getSavedProfilesUseCaseProvider);
  }

  public static LoginViewModel newInstance(LoginUserUseCase loginUserUseCase,
      GetSavedProfilesUseCase getSavedProfilesUseCase) {
    return new LoginViewModel(loginUserUseCase, getSavedProfilesUseCase);
  }
}
