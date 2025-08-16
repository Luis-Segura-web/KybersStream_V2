package com.kybers.stream.presentation.screens.settings;

import com.kybers.stream.domain.usecase.LogoutUserUseCase;
import com.kybers.stream.domain.usecase.preferences.GetUserPreferencesUseCase;
import com.kybers.stream.domain.usecase.preferences.UpdateUserPreferencesUseCase;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<GetUserPreferencesUseCase> getUserPreferencesUseCaseProvider;

  private final Provider<UpdateUserPreferencesUseCase> updateUserPreferencesUseCaseProvider;

  private final Provider<LogoutUserUseCase> logoutUserUseCaseProvider;

  private SettingsViewModel_Factory(
      Provider<GetUserPreferencesUseCase> getUserPreferencesUseCaseProvider,
      Provider<UpdateUserPreferencesUseCase> updateUserPreferencesUseCaseProvider,
      Provider<LogoutUserUseCase> logoutUserUseCaseProvider) {
    this.getUserPreferencesUseCaseProvider = getUserPreferencesUseCaseProvider;
    this.updateUserPreferencesUseCaseProvider = updateUserPreferencesUseCaseProvider;
    this.logoutUserUseCaseProvider = logoutUserUseCaseProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(getUserPreferencesUseCaseProvider.get(), updateUserPreferencesUseCaseProvider.get(), logoutUserUseCaseProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<GetUserPreferencesUseCase> getUserPreferencesUseCaseProvider,
      Provider<UpdateUserPreferencesUseCase> updateUserPreferencesUseCaseProvider,
      Provider<LogoutUserUseCase> logoutUserUseCaseProvider) {
    return new SettingsViewModel_Factory(getUserPreferencesUseCaseProvider, updateUserPreferencesUseCaseProvider, logoutUserUseCaseProvider);
  }

  public static SettingsViewModel newInstance(GetUserPreferencesUseCase getUserPreferencesUseCase,
      UpdateUserPreferencesUseCase updateUserPreferencesUseCase,
      LogoutUserUseCase logoutUserUseCase) {
    return new SettingsViewModel(getUserPreferencesUseCase, updateUserPreferencesUseCase, logoutUserUseCase);
  }
}
