package com.kybers.stream.di;

import android.content.Context;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.cache.SimpleCache;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "dagger.hilt.android.qualifiers.ApplicationContext",
    "com.kybers.stream.di.PlayerOkHttpClient"
})
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
public final class PlayerModule_ProvideDataSourceFactoryFactory implements Factory<DataSource.Factory> {
  private final Provider<Context> contextProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<SimpleCache> cacheProvider;

  private PlayerModule_ProvideDataSourceFactoryFactory(Provider<Context> contextProvider,
      Provider<OkHttpClient> okHttpClientProvider, Provider<SimpleCache> cacheProvider) {
    this.contextProvider = contextProvider;
    this.okHttpClientProvider = okHttpClientProvider;
    this.cacheProvider = cacheProvider;
  }

  @Override
  public DataSource.Factory get() {
    return provideDataSourceFactory(contextProvider.get(), okHttpClientProvider.get(), cacheProvider.get());
  }

  public static PlayerModule_ProvideDataSourceFactoryFactory create(
      Provider<Context> contextProvider, Provider<OkHttpClient> okHttpClientProvider,
      Provider<SimpleCache> cacheProvider) {
    return new PlayerModule_ProvideDataSourceFactoryFactory(contextProvider, okHttpClientProvider, cacheProvider);
  }

  public static DataSource.Factory provideDataSourceFactory(Context context,
      OkHttpClient okHttpClient, SimpleCache cache) {
    return Preconditions.checkNotNullFromProvides(PlayerModule.INSTANCE.provideDataSourceFactory(context, okHttpClient, cache));
  }
}
