package com.kyant.backdrop.catalog

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SeyraMusicTestActivity : Activity() {

    private lateinit var webView: WebView
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(MusicApiBridge(), "AndroidBridge")
            loadDataWithBaseURL(
                "https://yy.luodian.net.cn/",
                musicTestHtml,
                "text/html",
                "UTF-8",
                null
            )
        }

        setContentView(webView)
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private inner class MusicApiBridge {
        @JavascriptInterface
        fun searchMusic(input: String, type: String) {
            Thread {
                val result = runCatching {
                    val formBody = FormBody.Builder()
                        .add("input", input)
                        .add("type", type)
                        .build()
                    val request = Request.Builder()
                        .url("https://yy.luodian.net.cn/")
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) Chrome/104.0 Mobile Safari/537.36")
                        .post(formBody)
                        .build()

                    client.newCall(request).execute().use { response ->
                        val body = response.body?.string().orEmpty()
                        if (!response.isSuccessful) {
                            "请求失败：HTTP ${response.code}\n\n$body"
                        } else {
                            body.ifBlank { "请求成功，但返回内容为空" }
                        }
                    }
                }.getOrElse { error ->
                    "请求失败：${error.message ?: "未知错误"}"
                }

                val escaped = JSONObject.quote(result)
                runOnUiThread {
                    webView.evaluateJavascript("window.onNativeResult($escaped);", null)
                }
            }.start()
        }
    }

    companion object {
        private val musicTestHtml = """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>接口测试</title>
                <style>
                    *{box-sizing:border-box;margin:0;padding:0}
                    body{padding:20px;font-family:system-ui;background:#fff;color:#111827}
                    .item{margin:12px 0}
                    input,select{padding:10px;width:100%;font-size:16px;border:1px solid #d1d5db;border-radius:6px;background:#fff}
                    button{padding:12px;width:100%;background:#2563eb;color:#fff;border:none;border-radius:6px;font-size:16px}
                    button:active{opacity:.82}
                    #result{margin-top:16px;padding:12px;background:#f5f5f5;white-space:pre-wrap;word-break:break-all;border-radius:8px;min-height:180px;font-size:13px;line-height:1.5}
                    .tip{margin-top:10px;color:#6b7280;font-size:13px;line-height:1.5}
                </style>
            </head>
            <body>
                <div class="item">
                    <input id="inputText" placeholder="输入歌名/歌曲ID" value="晴天">
                </div>
                <div class="item">
                    <select id="typeSel">
                        <option value="netease">网易云 netease</option>
                        <option value="qq">QQ音乐 qq</option>
                        <option value="kugou">酷狗 kugou</option>
                        <option value="kuwo">酷我 kuwo</option>
                    </select>
                </div>
                <div class="item">
                    <button onclick="sendPost()">提交搜索</button>
                </div>
                <div class="tip">软件内会通过 Android 原生请求接口，不受浏览器跨域限制。</div>
                <div style="margin-top:16px;">返回结果：</div>
                <div id="result"></div>

            <script>
            function setResult(text){
                document.getElementById("result").innerText = text;
            }

            window.onNativeResult = function(text){
                setResult(text);
            }

            async function sendPost(){
                const url = "https://yy.luodian.net.cn/";
                const inputVal = document.getElementById("inputText").value;
                const typeVal = document.getElementById("typeSel").value;
                if(!inputVal.trim()){
                    setResult("请输入歌名/歌曲ID");
                    return;
                }
                setResult("请求中……");

                if(window.AndroidBridge && window.AndroidBridge.searchMusic){
                    window.AndroidBridge.searchMusic(inputVal,typeVal);
                    return;
                }

                const formData = new FormData();
                formData.append("input",inputVal);
                formData.append("type",typeVal);
                try{
                    const resp = await fetch(url,{
                        method:"POST",
                        body:formData,
                        headers:{
                            "User-Agent":"Mozilla/5.0 (Linux; Android 13) Chrome/104.0 Mobile Safari/537.36"
                        }
                    })
                    const htmlText = await resp.text();
                    setResult(htmlText);
                }catch(e){
                    setResult("请求失败："+e.message+"\n⚠️浏览器跨域拦截，属于正常现象，请用安卓代码测试接口");
                }
            }
            </script>
            </body>
            </html>
        """.trimIndent()
    }
}
