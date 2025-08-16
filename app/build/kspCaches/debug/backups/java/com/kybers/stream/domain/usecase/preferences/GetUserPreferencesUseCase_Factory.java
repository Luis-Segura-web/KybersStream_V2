package com.kybers.stream.domain.usecase.preferences;

import com.kybers.stream.domain.repository.UserPreferencesRepository;
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
public final class GetUserPreferencesUseCase_Factory implements Factory<GetUserPreferencesUseCase> {
  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  private GetUserPreferencesUseCase_Factory(
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
  }

  @Override
  public GetUserPreferencesUseCase get() {
    return newInstance(userPreferencesRepositoryProvider.get());
  }

  public static GetUserPreferencesUseCase_Factory create(
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    return new GetUserPreferencesUseCase_Factory(userPreferencesRepositoryProvider);
  }

  public static GetUserPreferencesUseCase newInstance(
      UserPreferencesRepository userPreferencesRepository) {
    return new GetUserPreferencesUseCase(userPreferencesRepository);
  }
}
