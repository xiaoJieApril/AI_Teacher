package com.lolha.learningapp.data.remote

import com.lolha.learningapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SupabaseSyncClient(
    private val deviceId: String,
    private val supabaseUrl: String = BuildConfig.SUPABASE_URL,
    private val anonKey: String = BuildConfig.SUPABASE_ANON_KEY,
) {
    private val enabled: Boolean = supabaseUrl.isNotBlank() && anonKey.isNotBlank()

    suspend fun upsert(table: String, payload: JSONObject): Result<Unit> = runRemote {
        val body = JSONObject(payload.toString()).put("device_id", deviceId)
        val connection = openConnection(
            path = "$table?on_conflict=remote_id",
            method = "POST",
        ).apply {
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Prefer", "resolution=merge-duplicates,return=minimal")
            doOutput = true
        }
        connection.outputStream.use { it.write(body.toString().toByteArray()) }
        requireSuccess(connection)
    }

    suspend fun delete(table: String, remoteId: String): Result<Unit> = runRemote {
        val connection = openConnection(
            path = "$table?remote_id=eq.${remoteId.url()}&device_id=eq.${deviceId.url()}",
            method = "DELETE",
        ).apply {
            setRequestProperty("Prefer", "return=minimal")
        }
        requireSuccess(connection)
    }

    suspend fun deleteByDevice(table: String): Result<Unit> = runRemote {
        val connection = openConnection(
            path = "$table?device_id=eq.${deviceId.url()}",
            method = "DELETE",
        ).apply {
            setRequestProperty("Prefer", "return=minimal")
        }
        requireSuccess(connection)
    }

    private suspend fun runRemote(block: suspend () -> Unit): Result<Unit> {
        if (!enabled) return Result.success(Unit)
        return withContext(Dispatchers.IO) {
            try {
                block()
                Result.success(Unit)
            } catch (error: Throwable) {
                Result.failure(error)
            }
        }
    }

    private fun openConnection(path: String, method: String): HttpURLConnection {
        val base = supabaseUrl.trimEnd('/')
        return (URL("$base/rest/v1/$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("apikey", anonKey)
            setRequestProperty("Authorization", "Bearer $anonKey")
            connectTimeout = 10_000
            readTimeout = 15_000
        }
    }

    private fun requireSuccess(connection: HttpURLConnection) {
        if (connection.responseCode in 200..299) return
        val error = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        throw IllegalStateException("Supabase ${connection.responseCode}: $error")
    }

    private fun String.url(): String = URLEncoder.encode(this, "UTF-8")
}
