import android.content.Context
import android.util.Base64
import android.util.Log
import java.io.*
import java.io.File

class LogcatThread(private val mContext: Context) : Thread() {

    @Volatile
    private var isRunning = false

    private val maxFileSizeBytes: Long = 1_000_000_000 // 1GB bytes

    override fun run() {
        isRunning = true
        while (isRunning) {
            saveLog()
            Thread.sleep(1000)
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

            val fileName = "logcat.log"
            val filePath = File(filesDir, fileName)

            //Condition for when the files should be deleted.
            if (filePath.exists() && filePath.length() > maxFileSizeBytes) {
                filePath.delete()
            }

            FileOutputStream(File(filesDir, fileName), true).bufferedWriter().use { writer ->
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null && isRunning && !isInterrupted) {
                    writer.write(line)
                    writer.newLine()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, MSG, e)
        }
    }

    companion object {
        private const val TAG = "LogcatThread"
        private const val MSG = "ERROR SAVE FILE: "
    }
}
