package com.example.starter.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.bio.npy.NpyFile.read
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.pytorch.IValue
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Paths
import kotlin.math.abs
import kotlin.math.pow


class Compute(val list: List<IValue>, val img_size: Int) {
    //    var size = mk[32, 64, 128, 256]
    lateinit var size: List<Int>
    var channel = mk[42, 33]
    var d_masks: ArrayList<D2Array<Int>> = ArrayList()
    var d_depths: ArrayList<D2Array<Int>> = ArrayList()
    var q_masks: ArrayList<D2Array<Int>> = ArrayList()
    var q_depths: ArrayList<D2Array<Int>> = ArrayList()
    var file1 = read(Paths.get(System.getProperty("user.dir") + "/" + "mask.npy"))
    var mask_color = mk.ndarray(file1.asIntArray(), file1.shape[0], file1.shape[1])
    var file2 = read(Paths.get(System.getProperty("user.dir") + "/" + "depth.npy"))
    var depth_color = mk.ndarray(file2.asIntArray(), file2.shape[0], file2.shape[1])

    suspend fun init() {

        var t_size: ArrayList<Int> = ArrayList()
        for (cnt in 0..3) {
            var t_pow = img_size / (2.0.pow(cnt).toInt())
            t_size.add(t_pow)
        }
        size = t_size.reversed()
        for (cnt2 in 0..7) {
            val data = list[cnt2].toTensor().dataAsFloatArray
            var result: ArrayList<Int> = ArrayList()
            var cnt = cnt2
            var cur_channel = channel[0]
            if (cnt2 > 3) {
                cnt = cnt2 - 4
                cur_channel = channel[1]
            }
            var height = size[cnt]
            var width = size[cnt]
            for (j in 0 until height) {
                for (k in 0 until width) {
                    var maxi = 0
                    var maxj = 0
                    var maxk = 0
                    var maxnum = -Double.MAX_VALUE
                    for (i in 0 until cur_channel) {
                        val score: Float = data[i * (width * height) + j * width + k]
                        if (score > maxnum) {
                            maxnum = score.toDouble()
                            maxi = i
                            maxj = j
                            maxk = k
                        }
                    }
                    if (maxi >= 1) {
                        result.add(maxi - 1)
                    } else {
                        result.add(maxi)
                    }
                }
            }
            if (cnt2 < 4) {
                var d_result = mk.ndarray(result).reshape(size[cnt], size[cnt])
                d_masks.add(d_result)
            } else {
                var d_result = mk.ndarray(result).reshape(size[cnt], size[cnt])
                d_depths.add(d_result)
            }
        }
//        quad tree
        for (cnt in 0..3) {
            var mask_t1 = d_masks[cnt]
            var depth_t1 = d_depths[cnt]
            if (cnt == 0) {
                q_masks.add(mask_t1)
                q_depths.add(depth_t1)
            } else {
                var mask_t0 = d_masks[cnt - 1]
                var depth_t0 = d_depths[cnt - 1]
                var t_size = size[cnt - 1]
                var mask_out = quad(mask_t0, mask_t1, t_size)
                var depth_out = quad(depth_t0, depth_t1, t_size)
                q_masks.add(mask_out)
                q_depths.add(depth_out)
            }
        }
    }

    suspend fun quad(
        t0: D2Array<Int>, t1: D2Array<Int>, size: Int
    ): D2Array<Int> = withContext(Dispatchers.Default) {
        val out = mk.zeros<Int>(t1.shape[0], t1.shape[1])
        for (i in 0 until size) {
            for (j in 0 until size) {
                for (a in 0 until 2) {
                    for (b in 0 until 2) {
                        val value = abs(t1[i * 2 + a, j * 2 + b] - t0[i, j])
                        out[i * 2 + a, j * 2 + b] = value
                    }
                }
            }
        }
        out
    }

    fun getmask(index: Int): BufferedImage {
        val rgb_array = d_masks[index]
        val bufferedImage = BufferedImage(size[index], size[index], BufferedImage.TYPE_INT_ARGB)
        for (i in 0 until size[index]) {
            for (j in 0 until size[index]) {
                val color = Color(
                    mask_color[rgb_array[i, j], 0], mask_color[rgb_array[i, j], 1], mask_color[rgb_array[i, j], 2]
                )
                bufferedImage.setRGB(j, i, color.rgb)
            }
        }
        return bufferedImage
    }

    fun getdepth(index: Int): BufferedImage {
        val rgb_array = d_depths[index]
        val bufferedImage = BufferedImage(size[index], size[index], BufferedImage.TYPE_INT_ARGB)
        for (i in 0 until size[index]) {
            for (j in 0 until size[index]) {
                val color = Color(
                    depth_color[rgb_array[i, j], 0], depth_color[rgb_array[i, j], 1], depth_color[rgb_array[i, j], 2]
                )
                bufferedImage.setRGB(j, i, color.rgb)
            }
        }
        return bufferedImage
    }

    suspend fun get_q_mask(index: Int): BufferedImage = withContext(Dispatchers.Default) {
        val rgb_array = q_masks[index]
        val bufferedImage = BufferedImage(size[index], size[index], BufferedImage.TYPE_INT_ARGB)
        for (i in 0 until size[index]) {
            for (j in 0 until size[index]) {
                val color = Color(
                    mask_color[rgb_array[i, j], 0], mask_color[rgb_array[i, j], 1], mask_color[rgb_array[i, j], 2]
                )
                bufferedImage.setRGB(j, i, color.rgb)
            }
        }
        return@withContext bufferedImage
    }

    suspend fun get_q_depth(index: Int): BufferedImage = withContext(Dispatchers.Default) {
        val rgb_array = q_depths[index]
        val bufferedImage = BufferedImage(size[index], size[index], BufferedImage.TYPE_INT_ARGB)
        for (i in 0 until size[index]) {
            for (j in 0 until size[index]) {
                val color = Color(
                    depth_color[rgb_array[i, j], 0], depth_color[rgb_array[i, j], 1], depth_color[rgb_array[i, j], 2]
                )
                bufferedImage.setRGB(j, i, color.rgb)
            }
        }
        return@withContext bufferedImage
    }

}

