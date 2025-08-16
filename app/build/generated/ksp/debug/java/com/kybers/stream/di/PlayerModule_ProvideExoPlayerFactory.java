package com.kybers.stream.di;

import android.content.Context;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.extractor.DefaultExtractorsFactory;
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
public final class PlayerModule_ProvideExoPlayerFactory implements Factory<ExoPlayer> {
  private final Provider<Context> contextProvider;

  private final Provider<DataSource.Factory> dataSourceFactoryProvider;

  private final Provider<LoadControl> loadControlProvider;

  private final Provider<DefaultExtractorsFactory> extractorsFactoryProvider;

  private PlayerModule_ProvideExoPlayerFactory(Provider<Context> contextProvider,
      Provider<DataSource.Factory> dataSourceFactoryProvider,
      Provider<LoadControl> loadControlProvider,
      Provider<DefaultExtractorsFactory> extractorsFactoryProvider) {
    this.contextProvider = contextProvider;
    this.dataSourceFactoryProvider = dataSourceFactoryProvider;
    this.loadControlProvider = loadControlProvider;
    this.extractorsFactoryProvider = extractorsFactoryProvider;
  }

  @Override
  public ExoPlayer get() {
    return provideExoPlayer(contextProvider.get(), dataSourceFactoryProvider.get(), loadControlProvider.get(), extractorsFactoryProvider.get());
  }

  public static PlayerModule_ProvideExoPlayerFactory create(Provider<Context> contextProvider,
      Provider<DataSource.Factory> dataSourceFactoryProvider,
      Provider<LoadControl> loadControlProvider,
      Provider<DefaultExtractorsFactory> extractorsFactoryProvider) {
    return new PlayerModule_ProvideExoPlayerFactory(contextProvider, dataSourceFactoryProvider, loadControlProvider, extractorsFactoryProvider);
  }

  public static ExoPlayer provideExoPlayer(Context context, DataSource.Factory dataSourceFactory,
      LoadControl loadControl, DefaultExtractorsFactory extractorsFactory) {
    return Preconditions.checkNotNullFromProvides(PlayerModule.INSTANCE.provideExoPlayer(context, dataSourceFactory, loadControl, extractorsFactory));
  }
}
