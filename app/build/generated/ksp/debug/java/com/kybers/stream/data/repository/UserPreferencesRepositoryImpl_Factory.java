package com.kybers.stream.data.repository;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class UserPreferencesRepositoryImpl_Factory implements Factory<UserPreferencesRepositoryImpl> {
  private final Provider<DataStore<Preferences>> dataStoreProvider;

  private UserPreferencesRepositoryImpl_Factory(
      Provider<DataStore<Preferences>> dataStoreProvider) {
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public UserPreferencesRepositoryImpl get() {
    return newInstance(dataStoreProvider.get());
  }

  public static UserPreferencesRepositoryImpl_Factory create(
      Provider<DataStore<Preferences>> dataStoreProvider) {
    return new UserPreferencesRepositoryImpl_Factory(dataStoreProvider);
  }

  public static UserPreferencesRepositoryImpl newInstance(DataStore<Preferences> dataStore) {
    return new UserPreferencesRepositoryImpl(dataStore);
  }
}
