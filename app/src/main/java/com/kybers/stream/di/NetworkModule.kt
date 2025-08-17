package com.kybers.stream.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kybers.stream.data.remote.api.XtreamApi
import com.kybers.stream.data.remote.api.TMDBApi
import com.kybers.stream.data.remote.interceptor.ExpiredAccountInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class XtreamRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TMDBRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val expiredAccountInterceptor = ExpiredAccountInterceptor()
        
        return OkHttpClient.Builder()
            .addInterceptor(expiredAccountInterceptor) // Debe ir antes del logging
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @XtreamRetrofit
    fun provideXtreamRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://example.com/") // Base URL temporal, se sobrescribirá por servidor
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    @TMDBRetrofit
    fun provideTMDBRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TMDBApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideXtreamApi(@XtreamRetrofit retrofit: Retrofit): XtreamApi {
        return retrofit.create(XtreamApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideTMDBApi(@TMDBRetrofit retrofit: Retrofit): TMDBApi {
        return retrofit.create(TMDBApi::class.java)
    }
}