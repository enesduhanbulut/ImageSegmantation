import com.example.starter.util.Compute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.imageio.ImageIO

@Throws(IOException::class)
suspend fun base64ToImage(data: String, width: Int, height: Int): BufferedImage = withContext(Dispatchers.IO) {
    val originalImage = getBufferedImage(data)
    createScaledBufferedImage(originalImage, width, height)
}

private suspend fun getBufferedImage(encoded: String): BufferedImage = withContext(Dispatchers.IO) {
    val imageBytes = Base64.getDecoder().decode(encoded)
    val bis = ByteArrayInputStream(imageBytes)
    ImageIO.read(bis)
}

suspend fun createScaledBufferedImage(
    bufferedImage: BufferedImage, width: Int, height: Int
): BufferedImage = withContext(Dispatchers.Default) {
    val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics = resizedImage.createGraphics()
    graphics.drawImage(bufferedImage, 0, 0, width, height, null)
    graphics.dispose()
    resizedImage
}

fun BufferedImage.toByteArray(imageFileType: String = "png"): String {
    val bas = ByteArrayOutputStream()
    return try {
        ImageIO.write(this, imageFileType, bas)
        val data = bas.toByteArray()
        val encoder = Base64.getEncoder()
        var ret = encoder.encodeToString(data)
        ret = ret.replace(System.lineSeparator(), "")
        ret
    } catch (e: Throwable) {
        throw RuntimeException()
    }
}

fun resourceFilePath(resource: String): Path {
    val resource: URL? = Compute::class.java.getResource("resource")
    return Paths.get(resource!!.toURI()).toFile().toPath()
}
