package com.zenith26.fest.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val gson = Gson()
    private var cachedService: ZenithApiService? = null

    val service: ZenithApiService
        get() {
            if (cachedService == null) {
                cachedService = createService(ApiConfig.baseUrl)
            }
            return cachedService!!
        }

    fun resetService() {
        cachedService = null
    }

    private fun createService(baseUrl: String): ZenithApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(ZenithApiService::class.java)
    }

    /**
     * Extracts the exact error message string from FastAPI error response body.
     * FastAPI sends `{"detail": "..."}` or `{"detail": [...]}`.
     */
    fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string() ?: return "Unknown server error (${response.code()})"
            val jsonObject = gson.fromJson(errorBody, JsonObject::class.java)
            if (jsonObject.has("detail")) {
                val detailElement = jsonObject.get("detail")
                if (detailElement.isJsonPrimitive) {
                    detailElement.asString
                } else if (detailElement.isJsonArray) {
                    val array = detailElement.asJsonArray
                    if (array.size() > 0 && array[0].isJsonObject) {
                        val firstObj = array[0].asJsonObject
                        firstObj.get("msg")?.asString ?: errorBody
                    } else {
                        errorBody
                    }
                } else {
                    errorBody
                }
            } else {
                errorBody
            }
        } catch (e: Exception) {
            "Error (${response.code()}): ${response.message()}"
        }
    }
}
