package com.kybers.stream.data.datasource;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
public final class OkHttpDataSourceFactory_Factory implements Factory<OkHttpDataSourceFactory> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private OkHttpDataSourceFactory_Factory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public OkHttpDataSourceFactory get() {
    return newInstance(okHttpClientProvider.get());
  }

  public static OkHttpDataSourceFactory_Factory create(
      Provider<OkHttpClient> okHttpClientProvider) {
    return new OkHttpDataSourceFactory_Factory(okHttpClientProvider);
  }

  public static OkHttpDataSourceFactory newInstance(OkHttpClient okHttpClient) {
    return new OkHttpDataSourceFactory(okHttpClient);
  }
}
