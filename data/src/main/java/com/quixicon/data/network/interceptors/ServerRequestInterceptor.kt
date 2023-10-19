package com.yymonitor.project.data.network.interceptors

/*
class ServerRequestInterceptor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val baseHost: String? = book.read(CacheKey.BACK_URL.name)

        val urlBeforeReplace: String = chain.request().url.toString()

        var urlAfterReplace = urlBeforeReplace.substringAfter("omnio.site:21784")
        val httpUrl: String? = "https://$baseHost$urlAfterReplace"

        val token: String? = book.read(CacheKey.ACCESS_TOKEN.name)
        val request = chain.request().newBuilder().apply {
            httpUrl?.let { url(it) }
            addHeader("Content-Type", "application/json")
            token?.also { addHeader("cookie", "access=${it}") }
        }
            .build()

        return chain.proceed(request)
    }
}
 */
