package com.quixicon.data.repositories.base

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.quixicon.domain.entities.error.ErrorDetails
import com.quixicon.domain.exceptions.Failure
import com.quixicon.domain.exceptions.FailureException
import io.reactivex.Single
import retrofit2.Response

abstract class BaseRepository {

    protected val gson: Gson = GsonBuilder()
        .setLenient()
        .excludeFieldsWithoutExposeAnnotation()
        .create()

    private fun <T> processError(response: Response<*>): Single<T> {
        val code = response.code()
        val errorBody = response.errorBody()
        return try {
            val json = errorBody.toString()
            val errorDetails = gson.fromJson(json, ErrorDetails::class.java)
            error(Failure.ServerError(code, errorDetails))
        } catch (e: JsonSyntaxException) {
            error(Failure.UnknownServerError(code, ErrorDetails(message = response.message())))
        }
    }

    private fun <T> success(value: T): Single<T> = Single.just(value)

    protected fun <T> error(failure: Failure, cause: Throwable? = null): Single<T> =
        Single.error(FailureException(failure, cause))

    @Suppress("UNCHECKED_CAST")
    protected fun <T> request(single: Single<Response<T>>): Single<T> =
        try {
            single.flatMap { response ->
                when (response.isSuccessful) {
                    true -> success(response.body() ?: Unit as T)
                    false -> processError(response)
                }
            }
        } catch (e: Throwable) {
            error(Failure.IoError, e)
        }

    protected fun <T, R> request(single: Single<Response<T>>, transform: ((T) -> R)): Single<R> =
        try {
            single.flatMap { response ->
                when (response.isSuccessful) {
                    true -> success(transform(response.body()!!))
                    false -> processError(response)
                }
            }
        } catch (e: Throwable) {
            error(Failure.IoError, e)
        }
}
