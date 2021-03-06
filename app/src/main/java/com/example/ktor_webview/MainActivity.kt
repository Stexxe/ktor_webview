package com.example.ktor_webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        copyWebResources()

        val server = initHttpServer()
        server.start()

        val webview = findViewById<WebView>(R.id.webview)
        initWebViewContent(webview)
    }

    private fun copyWebResources() {
        val files = assets.list("web")

        files?.forEach { path ->
            println(path)
            val input = assets.open("web/$path")
            val outFile = File(filesDir, path)
            val outStream = FileOutputStream(outFile)
            outStream.write(input.readBytes())
            outStream.close()
            input.close()
        }
    }

    private fun initHttpServer(): ApplicationEngine {
        return embeddedServer(Netty, 3333) {
            install(ContentNegotiation) {
                gson {}
            }
            routing {
                static("static") {
                    files(filesDir)
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewContent(webView: WebView) {
        webView.apply {
            loadUrl("http://127.0.0.1:3333/static/index.html")
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return false
                }
            }
            settings.apply {
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
            }
        }
    }
}