package com.yymonitor.project.data.network.interceptors

/*
class ServerResponseInterceptor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        response.headers("set-cookie").forEach {
            if (it.contains("access=")) {
                val token = it.replace(Regex(".*access=|;.*"), "")
                book.write(CacheKey.ACCESS_TOKEN.name, token)
            }
        }

        return response
    }
}
*/
