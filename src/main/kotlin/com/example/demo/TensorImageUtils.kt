package com.example.demo

import org.pytorch.MemoryFormat
import org.pytorch.Tensor
import java.awt.image.BufferedImage
import java.nio.FloatBuffer

/**
 * Contains utility functions for [org.pytorch.Tensor] creation from [ ] or [android.media.Image] source.
 */
object TensorImageUtils {
  var TORCHVISION_NORM_MEAN_RGB = floatArrayOf(0.485f, 0.456f, 0.406f)
  var TORCHVISION_NORM_STD_RGB = floatArrayOf(0.229f, 0.224f, 0.225f)

  /**
   * Creates new [org.pytorch.Tensor] from full [android.graphics.Bitmap], normalized
   * with specified in parameters mean and std.
   *
   * @param normMeanRGB means for RGB channels normalization, length must equal 3, RGB order
   * @param normStdRGB standard deviation for RGB channels normalization, length must equal 3, RGB
   * order
   */
  fun bitmapToFloat32Tensor(
    bitmap: BufferedImage,
    normMeanRGB: FloatArray,
    normStdRGB: FloatArray,
    memoryFormat: MemoryFormat
  ): Tensor {
    checkNormMeanArg(normMeanRGB)
    checkNormStdArg(normStdRGB)
    return bitmapToFloat32Tensor(
      bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), normMeanRGB, normStdRGB, memoryFormat
    )
  }

  fun bitmapToFloat32Tensor(
    bitmap: BufferedImage, normMeanRGB: FloatArray, normStdRGB: FloatArray
  ): Tensor {
    return bitmapToFloat32Tensor(
      bitmap,
      0,
      0,
      bitmap.getWidth(),
      bitmap.getHeight(),
      normMeanRGB,
      normStdRGB,
      MemoryFormat.CONTIGUOUS
    )
  }

  /**
   * Writes tensor content from specified [android.graphics.Bitmap], normalized with specified
   * in parameters mean and std to specified [java.nio.FloatBuffer] with specified offset.
   *
   * @param bitmap [android.graphics.Bitmap] as a source for Tensor data
   * @param x - x coordinate of top left corner of bitmap's area
   * @param y - y coordinate of top left corner of bitmap's area
   * @param width - width of bitmap's area
   * @param height - height of bitmap's area
   * @param normMeanRGB means for RGB channels normalization, length must equal 3, RGB order
   * @param normStdRGB standard deviation for RGB channels normalization, length must equal 3, RGB
   * order
   */
  fun bitmapToFloatBuffer(
    bitmap: BufferedImage,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    normMeanRGB: FloatArray,
    normStdRGB: FloatArray,
    outBuffer: FloatBuffer,
    outBufferOffset: Int,
    memoryFormat: MemoryFormat
  ) {
    checkOutBufferCapacity(outBuffer, outBufferOffset, width, height)
    checkNormMeanArg(normMeanRGB)
    checkNormStdArg(normStdRGB)
    require(!(memoryFormat !== MemoryFormat.CONTIGUOUS && memoryFormat !== MemoryFormat.CHANNELS_LAST)) { "Unsupported memory format $memoryFormat" }
    val pixelsCount = height * width
    val pixels = IntArray(pixelsCount)
    bitmap.getRGB(0, width, x, y, pixels, width, height)
    if (MemoryFormat.CONTIGUOUS === memoryFormat) {
      val offset_b = 2 * pixelsCount
      for (i in 0 until pixelsCount) {
        val c = pixels[i]
        val r = (c shr 16 and 0xff) / 255.0f
        val g = (c shr 8 and 0xff) / 255.0f
        val b = (c and 0xff) / 255.0f
        outBuffer.put(outBufferOffset + i, (r - normMeanRGB[0]) / normStdRGB[0])
        outBuffer.put(outBufferOffset + pixelsCount + i, (g - normMeanRGB[1]) / normStdRGB[1])
        outBuffer.put(outBufferOffset + offset_b + i, (b - normMeanRGB[2]) / normStdRGB[2])
      }
    } else {
      for (i in 0 until pixelsCount) {
        val c = pixels[i]
        val r = (c shr 16 and 0xff) / 255.0f
        val g = (c shr 8 and 0xff) / 255.0f
        val b = (c and 0xff) / 255.0f
        outBuffer.put(outBufferOffset + 3 * i + 0, (r - normMeanRGB[0]) / normStdRGB[0])
        outBuffer.put(outBufferOffset + 3 * i + 1, (g - normMeanRGB[1]) / normStdRGB[1])
        outBuffer.put(outBufferOffset + 3 * i + 2, (b - normMeanRGB[2]) / normStdRGB[2])
      }
    }
  }

  fun bitmapToFloatBuffer(
    bitmap: BufferedImage,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    normMeanRGB: FloatArray,
    normStdRGB: FloatArray,
    outBuffer: FloatBuffer,
    outBufferOffset: Int
  ) {
    bitmapToFloatBuffer(
      bitmap,
      x,
      y,
      width,
      height,
      normMeanRGB,
      normStdRGB,
      outBuffer,
      outBufferOffset,
      MemoryFormat.CONTIGUOUS
    )
  }

  /**
   * Creates new [org.pytorch.Tensor] from specified area of [android.graphics.Bitmap],
   * normalized with specified in parameters mean and std.
   *
   * @param bitmap [android.graphics.Bitmap] as a source for Tensor data
   * @param x - x coordinate of top left corner of bitmap's area
   * @param y - y coordinate of top left corner of bitmap's area
   * @param width - width of bitmap's area
   * @param height - height of bitmap's area
   * @param normMeanRGB means for RGB channels normalization, length must equal 3, RGB order
   * @param normStdRGB standard deviation for RGB channels normalization, length must equal 3, RGB
   * order
   */
  fun bitmapToFloat32Tensor(
    bitmap: BufferedImage,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    normMeanRGB: FloatArray,
    normStdRGB: FloatArray,
    memoryFormat: MemoryFormat
  ): Tensor {
    checkNormMeanArg(normMeanRGB)
    checkNormStdArg(normStdRGB)
    val floatBuffer: FloatBuffer = Tensor.allocateFloatBuffer(3 * width * height)
    bitmapToFloatBuffer(
      bitmap, x, y, width, height, normMeanRGB, normStdRGB, floatBuffer, 0, memoryFormat
    )
    return Tensor.fromBlob(
      floatBuffer,
      longArrayOf(1, 3, height.toLong(), width.toLong()),
      memoryFormat
    )
  }

  fun bitmapToFloat32Tensor(
    bitmap: BufferedImage,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    normMeanRGB: FloatArray,
    normStdRGB: FloatArray
  ): Tensor {
    return bitmapToFloat32Tensor(
      bitmap, x, y, width, height, normMeanRGB, normStdRGB, MemoryFormat.CONTIGUOUS
    )
  }

  /**
   * Creates new [org.pytorch.Tensor] from specified area of [android.media.Image],
   * doing optional rotation, scaling (nearest) and center cropping.
   *
   * @param image [android.media.Image] as a source for Tensor data
   * @param rotateCWDegrees Clockwise angle through which the input image needs to be rotated to be
   * upright. Range of valid values: 0, 90, 180, 270
   * @param tensorWidth return tensor width, must be positive
   * @param tensorHeight return tensor height, must be positive
   * @param normMeanRGB means for RGB channels normalization, length must equal 3, RGB order
   * @param normStdRGB standard deviation for RGB channels normalization, length must equal 3, RGB
   * order
   */

  /**
   * Writes tensor content from specified [android.media.Image], doing optional rotation,
   * scaling (nearest) and center cropping to specified [java.nio.FloatBuffer] with specified
   * offset.
   *
   * @param image [android.media.Image] as a source for Tensor data
   * @param rotateCWDegrees Clockwise angle through which the input image needs to be rotated to be
   * upright. Range of valid values: 0, 90, 180, 270
   * @param tensorWidth return tensor width, must be positive
   * @param tensorHeight return tensor height, must be positive
   * @param normMeanRGB means for RGB channels normalization, length must equal 3, RGB order
   * @param normStdRGB standard deviation for RGB channels normalization, length must equal 3, RGB
   * order
   * @param outBuffer Output buffer, where tensor content will be written
   * @param outBufferOffset Output buffer offset with which tensor content will be written
   */

  private fun checkOutBufferCapacity(
    outBuffer: FloatBuffer, outBufferOffset: Int, tensorWidth: Int, tensorHeight: Int
  ) {
    check(outBufferOffset + 3 * tensorWidth * tensorHeight <= outBuffer.capacity()) { "Buffer underflow" }
  }

  private fun checkTensorSize(tensorWidth: Int, tensorHeight: Int) {
    require(!(tensorHeight <= 0 || tensorWidth <= 0)) { "tensorHeight and tensorWidth must be positive" }
  }

  private fun checkRotateCWDegrees(rotateCWDegrees: Int) {
    require(!(rotateCWDegrees != 0 && rotateCWDegrees != 90 && rotateCWDegrees != 180 && rotateCWDegrees != 270)) { "rotateCWDegrees must be one of 0, 90, 180, 270" }
  }

  private fun checkNormStdArg(normStdRGB: FloatArray) {
    require(normStdRGB.size == 3) { "normStdRGB length must be 3" }
  }

  private fun checkNormMeanArg(normMeanRGB: FloatArray) {
    require(normMeanRGB.size == 3) { "normMeanRGB length must be 3" }
  }

}
