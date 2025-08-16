package com.kybers.stream.di;

import android.content.Context;
import com.kybers.stream.data.local.database.KybersStreamDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideKybersStreamDatabaseFactory implements Factory<KybersStreamDatabase> {
  private final Provider<Context> contextProvider;

  private DatabaseModule_ProvideKybersStreamDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public KybersStreamDatabase get() {
    return provideKybersStreamDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideKybersStreamDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideKybersStreamDatabaseFactory(contextProvider);
  }

  public static KybersStreamDatabase provideKybersStreamDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideKybersStreamDatabase(context));
  }
}
