package com.kybers.stream.di;

import androidx.media3.extractor.DefaultExtractorsFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class PlayerModule_ProvideExtractorsFactoryFactory implements Factory<DefaultExtractorsFactory> {
  @Override
  public DefaultExtractorsFactory get() {
    return provideExtractorsFactory();
  }

  public static PlayerModule_ProvideExtractorsFactoryFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DefaultExtractorsFactory provideExtractorsFactory() {
    return Preconditions.checkNotNullFromProvides(PlayerModule.INSTANCE.provideExtractorsFactory());
  }

  private static final class InstanceHolder {
    static final PlayerModule_ProvideExtractorsFactoryFactory INSTANCE = new PlayerModule_ProvideExtractorsFactoryFactory();
  }
}
