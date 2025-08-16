package com.kybers.stream.data.datasource

import androidx.media3.datasource.DataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OkHttpDataSourceFactory @Inject constructor(
    private val okHttpClient: OkHttpClient
) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("KybersStream/1.0")
            .setCacheControl(okhttp3.CacheControl.FORCE_NETWORK)
            .createDataSource()
    }
}

class CacheableOkHttpDataSourceFactory @Inject constructor(
    private val okHttpClient: OkHttpClient
) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("KybersStream/1.0")
            .createDataSource()
    }
}