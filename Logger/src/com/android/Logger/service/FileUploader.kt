import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class FileUploader {

    sealed class Result {
        object Success : Result()
        data class Failure(val exception: Exception) : Result()
    }

    fun uploadFile(file: File, urlString: String): Result {
        val boundary = "===" + System.currentTimeMillis() + "==="
        val lineEnd = "\r\n"
        val twoHyphens = "--"

        var connection: HttpURLConnection? = null
        var outputStream: DataOutputStream? = null

        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.apply {
                doOutput = true
                doInput = true
                requestMethod = "POST"
                setRequestProperty("Connection", "Keep-Alive")
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            }

            outputStream = DataOutputStream(connection.outputStream).apply {
                writeBytes(twoHyphens + boundary + lineEnd)
                writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"$lineEnd")
                writeBytes(lineEnd)
            }

            FileInputStream(file).use { inputStream ->
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream!!.write(buffer, 0, bytesRead)
                }
            }

            outputStream!!.apply {
                writeBytes(lineEnd)
                writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                flush()
                close()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Result.Success
            } else {
                val errorStream = connection.errorStream ?: connection.inputStream
                val response = errorStream.bufferedReader().use(BufferedReader::readText)
                Result.Failure(IOException("HTTP error: $responseCode - $response"))
            }
        } catch (e: Exception) {
            Result.Failure(e)
        } finally {
            try {
                outputStream?.close()
                connection?.disconnect()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

