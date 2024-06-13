import android.content.Context
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.provider.Settings
import android.provider.Settings.System



class LogcatThread(private val mContext: Context) : Thread() {

    @Volatile
    private var isRunning = false

    private val maxFileSizeBytes: Long = 500_000 // 500KB
    private val sleepInterval: Long = 5000 // 5 seconds

    override fun run() {
        isRunning = true
        while (isRunning) {
            saveLog()
            Thread.sleep(sleepInterval)
        }
    }

    fun stopRunning() {
        isRunning = false
        interrupt()
    }

    private fun saveLog() {
        try {
            val process = Runtime.getRuntime().exec("logcat")
            val bufferedReader = process.inputStream.bufferedReader()

            val filesDir = mContext.filesDir
            val androidId = getAndroidID()
            var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            var fileName = "logcat_$timeStamp$androidId.log"
            var logFile = File(filesDir, fileName)

            var writer = FileOutputStream(logFile, true).bufferedWriter()

            var line: String?
            while (bufferedReader.readLine().also { line = it } != null && isRunning && !isInterrupted) {
                writer.write(line)
                writer.newLine()
                writer.flush()

                if (logFile.length() > maxFileSizeBytes) {
                    writer.close()

                    // Upload and delete the log file
                    uploadAndDeleteFile(logFile)
                    logFile.delete()
                    
                    
                    // Start a new log file on
                    timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    fileName = "logcat_$timeStamp$androidId.log"
                    logFile = File(filesDir, fileName)
                    writer = FileOutputStream(logFile, true).bufferedWriter()
                }
            }
            writer.close()
        } catch (e: Exception) {
            Log.e(TAG, MSG, e)
        }
    }

    private fun uploadAndDeleteFile(file: File) {
    
        val fileUploader = FileUploader()
        val url = "https://market.agapengo.com/logs/upload_log.php"
        val result = fileUploader.uploadFile(file, url)

        if (result is FileUploader.Result.Success) {
            println("File uploaded successfully!")
            Log.i("UploadFile", "Successfully done", )
        } else if (result is FileUploader.Result.Failure) {
            println("File upload failed: ${(result as FileUploader.Result.Failure).exception.message}")
            Log.e("UploadFile", "${(result as FileUploader.Result.Failure).exception.message}", )
        }

    }
    
    
    //Function for getting IMEI
    private fun getAndroidID(): String? {
        return Settings.Secure.getString(mContext.contentResolver, Settings.Secure.ANDROID_ID)
    }

    companion object {
        private const val TAG = "LogcatThread"
        private const val MSG = "ERROR SAVE FILE: "
    }
}

