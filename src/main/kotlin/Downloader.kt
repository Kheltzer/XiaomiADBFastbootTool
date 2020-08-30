import javafx.scene.control.Label
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels

class Downloader(link: String, private val target: File, private val progressLabel: Label) {

    private val url = URL(link)
    private val size = url.openConnection().contentLengthLong.toFloat()
    private var startTime = 0L
    private val progress: Float
        get() = (target.length() / size) * 100f
    private val speed: Float
        get() = target.length() / ((System.currentTimeMillis() - startTime) / 1000.0f)

    suspend fun start(scope: CoroutineScope = GlobalScope) {
        startTime = System.currentTimeMillis()
        val job = scope.launch(Dispatchers.IO) {
            FileOutputStream(target).channel.transferFrom(
                Channels.newChannel(url.openStream()),
                0,
                Long.MAX_VALUE
            )
        }
        while (!job.isCompleted) {
            val speed = speed / 1000f
            val progress = progress.toString().take(4)
            withContext(Dispatchers.Main) {
                progressLabel.text = if (speed < 1000f)
                    "$progress %\t${speed.toString().take(5)} KB/s"
                else "$progress %\t${(speed / 1000f).toString().take(5)} MB/s"
            }
            delay(1000)
        }
    }
}