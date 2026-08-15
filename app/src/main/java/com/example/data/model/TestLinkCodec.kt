package com.example.data.model

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Builds and reads self-contained KARAM CBT test-link URIs. */
object TestLinkCodec {
    private const val APP_SCHEME = "karam"
    private const val APP_HOST = "test"
    private const val WEB_HOST = "karam-2027.test"
    private const val WEB_PREFIX = "/t/"

    fun build(payload: TestLinkPayload): String =
        "https://$WEB_HOST$WEB_PREFIX" +
            URLEncoder.encode(payload.encode(), StandardCharsets.UTF_8.toString())

    fun tokenFromUri(uri: android.net.Uri): String? {
        return when {
            uri.scheme == APP_SCHEME && uri.host == APP_HOST ->
                uri.pathSegments.lastOrNull()?.takeIf { it.isNotBlank() }
            uri.scheme == "https" && uri.host == WEB_HOST && uri.path?.startsWith(WEB_PREFIX) == true ->
                uri.pathSegments.lastOrNull()?.takeIf { it.isNotBlank() }
            else -> null
        }
    }
}
