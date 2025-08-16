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
public final class LoginUserUseCase_Factory implements Factory<LoginUserUseCase> {
  private final Provider<UserRepository> userRepositoryProvider;

  private LoginUserUseCase_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public LoginUserUseCase get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static LoginUserUseCase_Factory create(Provider<UserRepository> userRepositoryProvider) {
    return new LoginUserUseCase_Factory(userRepositoryProvider);
  }

  public static LoginUserUseCase newInstance(UserRepository userRepository) {
    return new LoginUserUseCase(userRepository);
  }
}
