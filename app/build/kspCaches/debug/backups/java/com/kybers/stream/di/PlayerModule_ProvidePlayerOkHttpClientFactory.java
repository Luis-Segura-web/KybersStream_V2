package com.kybers.stream.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.kybers.stream.di.PlayerOkHttpClient")
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
public final class PlayerModule_ProvidePlayerOkHttpClientFactory implements Factory<OkHttpClient> {
  @Override
  public OkHttpClient get() {
    return providePlayerOkHttpClient();
  }

  public static PlayerModule_ProvidePlayerOkHttpClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OkHttpClient providePlayerOkHttpClient() {
    return Preconditions.checkNotNullFromProvides(PlayerModule.INSTANCE.providePlayerOkHttpClient());
  }

  private static final class InstanceHolder {
    static final PlayerModule_ProvidePlayerOkHttpClientFactory INSTANCE = new PlayerModule_ProvidePlayerOkHttpClientFactory();
  }
}
