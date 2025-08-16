package com.kybers.stream.data.repository;

import com.kybers.stream.domain.repository.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
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
public final class XtreamRepositoryImpl_Factory implements Factory<XtreamRepositoryImpl> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<Retrofit> retrofitProvider;

  private XtreamRepositoryImpl_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<Retrofit> retrofitProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public XtreamRepositoryImpl get() {
    return newInstance(userRepositoryProvider.get(), retrofitProvider.get());
  }

  public static XtreamRepositoryImpl_Factory create(Provider<UserRepository> userRepositoryProvider,
      Provider<Retrofit> retrofitProvider) {
    return new XtreamRepositoryImpl_Factory(userRepositoryProvider, retrofitProvider);
  }

  public static XtreamRepositoryImpl newInstance(UserRepository userRepository, Retrofit retrofit) {
    return new XtreamRepositoryImpl(userRepository, retrofit);
  }
}
