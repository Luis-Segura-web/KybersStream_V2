package com.kybers.stream.di;

import androidx.media3.exoplayer.LoadControl;
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
public final class PlayerModule_ProvideLoadControlFactory implements Factory<LoadControl> {
  @Override
  public LoadControl get() {
    return provideLoadControl();
  }

  public static PlayerModule_ProvideLoadControlFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LoadControl provideLoadControl() {
    return Preconditions.checkNotNullFromProvides(PlayerModule.INSTANCE.provideLoadControl());
  }

  private static final class InstanceHolder {
    static final PlayerModule_ProvideLoadControlFactory INSTANCE = new PlayerModule_ProvideLoadControlFactory();
  }
}
