package com.example.ktor_webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        copyWebResources("web", filesDir)

        embeddedServer(Netty, 3333) {
            routing {
                static("static") {
                    files(filesDir)
                }
            }
        }.start(wait = false)

        val webView = findViewById<WebView>(R.id.webview)
        initWebViewContent(webView)
    }

    private fun copyWebResources(assetDir: String, outDir: File) {
        val files = assets.list(assetDir) ?: return

        for (path in files) {
            val outFile = File(outDir, path)
            val assetPath = "$assetDir/$path"

            val input = try {
                assets.open(assetPath).buffered()
            } catch (_: FileNotFoundException) {
                // Seems like path points to a directory
                outFile.mkdir()
                copyWebResources(assetPath, File(outDir, path))
                continue
            }

            FileOutputStream(outFile).use { stream ->
                stream.write(input.readBytes())
            }

            input.close()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewContent(webView: WebView) {
        webView.apply {
            loadUrl("http://127.0.0.1:3333/static/index.html")
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    view?.loadUrl(request?.url.toString())
                    return false
                }
            }
            settings.apply {
                setSupportZoom(true)
                domStorageEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
            }
        }
    }
}