package com.example.data.model

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Builds a KARAM test-link URL containing an encoded saved test payload. */
object TestLinkCodec {
    private const val PREFIX = "https://karam-2027.test/t/"

    fun build(payload: TestLinkPayload): String = PREFIX + URLEncoder.encode(payload.encode(), StandardCharsets.UTF_8.toString())

    fun tokenFromUri(uri: android.net.Uri): String? {
        if (uri.scheme != "https" || uri.host != "karam-2027.test") return null
        return uri.pathSegments.lastOrNull()?.takeIf { it.isNotBlank() }
    }
}
