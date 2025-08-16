package com.kybers.stream.di;

import com.kybers.stream.data.local.dao.UserPreferencesDao;
import com.kybers.stream.data.local.database.KybersStreamDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideUserPreferencesDaoFactory implements Factory<UserPreferencesDao> {
  private final Provider<KybersStreamDatabase> databaseProvider;

  private DatabaseModule_ProvideUserPreferencesDaoFactory(
      Provider<KybersStreamDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public UserPreferencesDao get() {
    return provideUserPreferencesDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideUserPreferencesDaoFactory create(
      Provider<KybersStreamDatabase> databaseProvider) {
    return new DatabaseModule_ProvideUserPreferencesDaoFactory(databaseProvider);
  }

  public static UserPreferencesDao provideUserPreferencesDao(KybersStreamDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideUserPreferencesDao(database));
  }
}
