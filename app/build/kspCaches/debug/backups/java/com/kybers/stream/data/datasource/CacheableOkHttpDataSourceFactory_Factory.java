package com.kybers.stream.data.datasource;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
public final class CacheableOkHttpDataSourceFactory_Factory implements Factory<CacheableOkHttpDataSourceFactory> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private CacheableOkHttpDataSourceFactory_Factory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public CacheableOkHttpDataSourceFactory get() {
    return newInstance(okHttpClientProvider.get());
  }

  public static CacheableOkHttpDataSourceFactory_Factory create(
      Provider<OkHttpClient> okHttpClientProvider) {
    return new CacheableOkHttpDataSourceFactory_Factory(okHttpClientProvider);
  }

  public static CacheableOkHttpDataSourceFactory newInstance(OkHttpClient okHttpClient) {
    return new CacheableOkHttpDataSourceFactory(okHttpClient);
  }
}
