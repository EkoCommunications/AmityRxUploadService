package com.ekoapp.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.ekoapp.rxuploadservice.RxUploadService
import com.ekoapp.rxuploadservice.extension.upload
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var accessToken: String

    private val id = UUID.randomUUID().toString()
    private val deviceId = UUID.randomUUID().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RxUploadService.init("https://sea-staging-h1.ekoapp.com")

        findViewById<AppCompatButton>(R.id.sign_in_button)
            .setOnClickListener {
                Maybe.create<String> {
                    val json = JsonObject()
                    json.addProperty("username", "c10")
                    json.addProperty("password", "password")
                    json.addProperty("deviceId", deviceId)
                    json.addProperty("deviceVersion", "android")
                    json.addProperty("deviceType", "android")
                    json.addProperty("deviceModel", "android")
                    json.addProperty("appId", "android")

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = json.toString().toRequestBody(mediaType)

                    val request: okhttp3.Request = okhttp3.Request.Builder()
                        .addHeader("Content-Type", "application/json")
                        .url("https://sea-staging-h1.ekoapp.com/api/v1/auth/login")
                        .post(body)
                        .build()

                    val client = OkHttpClient()
                    val response = client.newCall(request)
                        .execute()

                    val jsonElement = JsonParser.parseString(response.body?.string())
                    val accessToken = jsonElement.asJsonObject
                        .get("accessToken")
                        .asString

                    it.onSuccess(accessToken)
                    it.onComplete()
                }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess {
                        accessToken = it
                        findViewById<AppCompatButton>(R.id.sign_in_button).isEnabled = false
                        findViewById<AppCompatButton>(R.id.open_document_button).isEnabled = true
                        findViewById<AppCompatButton>(R.id.get_content_button).isEnabled = true
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
            }

        findViewById<AppCompatButton>(R.id.open_document_button)
            .setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, 0)
            }

        findViewById<AppCompatButton>(R.id.get_content_button)
            .setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, 0)
            }

        findViewById<AppCompatButton>(R.id.cancel_button)
            .setOnClickListener {
                RxUploadService.cancel(id)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                uri.upload(
                    context = this,
                    path = "file/upload-file",
                    headers = mapOf("x-eko-access-token" to accessToken),
                    id = id
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        findViewById<AppCompatTextView>(R.id.progress_text_view).text =
                            String.format("%s/100", it.progress)
                    }
                    .doOnNext { Log.e("testtest", "doOnNext:" + Gson().toJson(it)) }
                    .doOnComplete { Log.e("testtest", "doOnComplete") }
                    .doOnError { Log.e("testtest", "doOnError:" + it.message) }
                    .subscribeOn(Schedulers.io())
                    .subscribe({}, {})

                RxUploadService.properties(id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        findViewById<AppCompatTextView>(R.id.progress_text_view_2).text =
                            String.format("%s/100", it.progress)
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribe({}, {})
            }
        }
    }
}