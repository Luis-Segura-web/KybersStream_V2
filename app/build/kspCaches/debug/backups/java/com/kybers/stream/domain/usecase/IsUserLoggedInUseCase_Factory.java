package com.kybers.stream.domain.usecase;

import com.kybers.stream.domain.repository.UserRepository;
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
public final class IsUserLoggedInUseCase_Factory implements Factory<IsUserLoggedInUseCase> {
  private final Provider<UserRepository> userRepositoryProvider;

  private IsUserLoggedInUseCase_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public IsUserLoggedInUseCase get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static IsUserLoggedInUseCase_Factory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new IsUserLoggedInUseCase_Factory(userRepositoryProvider);
  }

  public static IsUserLoggedInUseCase newInstance(UserRepository userRepository) {
    return new IsUserLoggedInUseCase(userRepository);
  }
}
