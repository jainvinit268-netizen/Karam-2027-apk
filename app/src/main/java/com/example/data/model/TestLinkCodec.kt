package com.example.data.model

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Builds a self-contained KARAM CBT test-link URI. */
object TestLinkCodec {
    private const val PREFIX = "karam://test/"

    fun build(payload: TestLinkPayload): String =
        PREFIX + URLEncoder.encode(payload.encode(), StandardCharsets.UTF_8.toString())

    fun tokenFromUri(uri: android.net.Uri): String? {
        if (uri.scheme != "karam" || uri.host != "test") return null
        return uri.pathSegments.lastOrNull()?.takeIf { it.isNotBlank() }
    }
}
