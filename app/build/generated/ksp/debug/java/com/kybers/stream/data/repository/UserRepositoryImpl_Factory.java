package com.kybers.stream.data.repository;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
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
public final class UserRepositoryImpl_Factory implements Factory<UserRepositoryImpl> {
  private final Provider<DataStore<Preferences>> dataStoreProvider;

  private final Provider<Retrofit> retrofitProvider;

  private UserRepositoryImpl_Factory(Provider<DataStore<Preferences>> dataStoreProvider,
      Provider<Retrofit> retrofitProvider) {
    this.dataStoreProvider = dataStoreProvider;
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public UserRepositoryImpl get() {
    return newInstance(dataStoreProvider.get(), retrofitProvider.get());
  }

  public static UserRepositoryImpl_Factory create(
      Provider<DataStore<Preferences>> dataStoreProvider, Provider<Retrofit> retrofitProvider) {
    return new UserRepositoryImpl_Factory(dataStoreProvider, retrofitProvider);
  }

  public static UserRepositoryImpl newInstance(DataStore<Preferences> dataStore,
      Retrofit retrofit) {
    return new UserRepositoryImpl(dataStore, retrofit);
  }
}
