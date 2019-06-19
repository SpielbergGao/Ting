package com.zjw.ting.util

/**
 * Copyright (c) 2012-2013, Michael Yang 杨福海 (www.yangfuhai.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * 项目 数据缓存工具类，可以缓存jsonObject bitmap Object String drawable byte jsonarray
 * 可自定义缓存时间
 */
class ACache private constructor(cacheDir: File, max_size: Long, max_count: Int) {

    private val mCache: ACacheManager

    init {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw RuntimeException("can't make dirs in " + cacheDir.absolutePath)
        }
        mCache = ACacheManager(cacheDir, max_size, max_count)
    }

    //判断本地数据是否存在
    fun isExist(Cachekey: String, Datatype: String, context: Context): Boolean {
        if (!TextUtils.isEmpty(Datatype)) {
            when (Datatype) {
                STRING -> if (!TextUtils.isEmpty(getAsString(Cachekey))) return true
                Binary -> if (getAsBinary(Cachekey) != null) return true
                Bitmap -> if (getAsBitmap(Cachekey) != null) return true
                Drawable -> if (getAsDrawable(Cachekey, context) != null) return true
                Object -> if (getAsObject(Cachekey) != null) return true
                jsonArray -> if (getAsJSONArray(Cachekey) != null) return true
                jsonObject -> if (getAsJSONObject(Cachekey) != null) return true
            }
        }
        return false
    }

    /**
     * Provides a means to save a cached file before the data are available.
     * Since writing about the file is complete, and its close method is called,
     * its contents will be registered in the cache. Example of use:
     *
     * ACache cache = new ACache(this) try { OutputStream stream =
     * cache.put("myFileName") stream.write("some bytes".getBytes()); // now
     * update cache! stream.close(); } catch(FileNotFoundException e){
     * e.printStackTrace() }
     */
    internal inner class xFileOutputStream @Throws(FileNotFoundException::class)

    constructor(var file: File) : FileOutputStream(file) {
        @Throws(IOException::class)
        override fun close() {
            super.close()
            mCache.put(file)
        }
    }

    // =======================================
    // ============ String数据 读写 ==============
    // =======================================
    /**
     * 保存 String数据 到 缓存中
     *
     * @param key
     * 保存的key
     * @param value
     * 保存的String数据
     */
    fun onStringPutCache(key: String, value: String) {
        val file = mCache.newFile(key)
        var out: BufferedWriter? = null
        try {
            out = BufferedWriter(FileWriter(file), 1024)
            out.write(value)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (out != null) {
                try {
                    out.flush()
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            mCache.put(file)
        }
    }

    /**
     * 保存 String数据 到 缓存中
     *
     * @param key
     * 保存的key
     * @param value
     * 保存的String数据
     * @param saveTime
     * 保存的时间，单位：秒
     */
    fun onStringPutCache(key: String, value: String, saveTime: Int) {
        onStringPutCache(key, Utils.newStringWithDateInfo(saveTime, value))
    }

    /**
     * 读取 String数据
     *
     * @param key
     * @return String 数据
     */
    fun getAsString(key: String): String? {

        val file = mCache[key]

        if (!file.exists()) {
            return null
        }

        var removeFile = false

        var mBufferedReader: BufferedReader? = null

        try {
            mBufferedReader = BufferedReader(FileReader(file))

            val readString = StringBuilder()

            val currentLine: String = mBufferedReader.readLine()

            while (currentLine.isNotEmpty()) {
                readString.append(currentLine)
            }
            return if (!Utils.isDue(readString.toString())) {
                Utils.clearDateInfo(readString.toString())
            } else {
                removeFile = true
                null
            }

        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            try {
                mBufferedReader?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (removeFile) {
                remove(key)
            }

        }
    }

    // =======================================
    // ============= JSONObject 数据 读写 ==============
    // =======================================
    /**
     * 保存 JSONObject数据 到 缓存中
     *
     * @param key
     * 保存的key
     * @param value
     * 保存的JSON数据
     */
    fun onJSONObjectPutCache(key: String, value: JSONObject) {
        onStringPutCache(key, value.toString())
    }

    /**
     * 保存 JSONObject数据 到 缓存中
     *
     * @param key
     * 保存的key
     * @param value
     * 保存的JSONObject数据
     * @param saveTime
     * 保存的时间，单位：秒
     */
    fun onJSONObjectPutCache(key: String, value: JSONObject, saveTime: Int) {
        onStringPutCache(key, value.toString(), saveTime)
    }

    /**
     * 读取JSONObject数据
     *
     * @param key
     * @return JSONObject数据
     */
    fun getAsJSONObject(key: String): JSONObject? {

        val JSONString = getAsString(key)

        if (!TextUtils.isEmpty(JSONString)) {
            return try {
                JSONObject(JSONString)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

        }
        return null
    }

    // =======================================
    // ============ JSONArray 数据 读写 =============
    // =======================================
    /**
     * 保存 JSONArray数据 到 缓存中
     *
     * @param key
     * 保存的key
     * @param value
     * 保存的JSONArray数据
     */
    fun onJSONArrayPutCache(key: String, value: JSONArray) {
        onStringPutCache(key, value.toString())
    }

    /**
     * 保存 JSONArray数据 到 缓存中
     *
     * @param key
     * 保存的key
     * @param value
     * 保存的JSONArray数据
     * @param saveTime
     * 保存的时间，单位：秒
     */
    fun onJSONArrayPutCache(key: String, value: JSONArray, saveTime: Int) {
        onStringPutCache(key, value.toString(), saveTime)
    }

    /**
     * 读取JSONArray数据
     *
     * @param key
     * @return JSONArray数据
     */
    fun getAsJSONArray(key: String): JSONArray? {
        val JSONString = getAsString(key)
        return try {
            JSONArray(JSONString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    // =======================================
    // ============== byte 数据 读写 =============
    // =======================================
    /**
     * 保存 byte数据 到 缓存中
     *
     * @param key
     * 保存的key
     * @param value
     * 保存的数据
     */
    fun onBytePutCache(key: String, value: ByteArray?) {
        val file = mCache.newFile(key)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            out.write(value)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

            try {
                out?.flush()
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mCache.put(file)
        }
    }

    /**
     * Cache for a stream
     *
     * @param key
     * the file name.
     * @return OutputStream stream for writing data.
     * @throws FileNotFoundException
     * if the file can not be created.
     */
    @Throws(FileNotFoundException::class)
    fun put(key: String): OutputStream {
        return xFileOutputStream(mCache.newFile(key))
    }

    /**
     *
     * @param key
     * the file name.
     * @return (InputStream or null) stream previously saved in cache.
     * @throws FileNotFoundException
     * if the file can not be opened
     */
    @Throws(FileNotFoundException::class)
    operator fun get(key: String): InputStream? {
        val file = mCache[key]
        return if (!file.exists()) null else FileInputStream(file)
    }

    /**
     * 保存 byte数据 到 缓存中
     *
     * @param key
     * 保存的key
     * @param value
     * 保存的数据
     * @param saveTime
     * 保存的时间，单位：秒
     */
    fun onBytePutCache(key: String, value: ByteArray?, saveTime: Int) {
        onBytePutCache(key, Utils.newByteArrayWithDateInfo(saveTime, value!!))
    }

    /**
     * 获取 byte 数据
     *
     * @param key
     * @return byte 数据
     */
    fun getAsBinary(key: String): ByteArray? {
        var RAFile: RandomAccessFile? = null
        var removeFile = false
        try {
            val file = mCache.get(key)
            if (!file.exists())
                return null
            RAFile = RandomAccessFile(file, "r")
            val byteArray = ByteArray(RAFile.length().toInt())
            RAFile.read(byteArray)
            return if (!Utils.isDue(byteArray)) {
                Utils.clearDateInfo(byteArray)
            } else {
                removeFile = true
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            if (RAFile != null) {
                try {
                    RAFile.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (removeFile)
                remove(key)
        }
    }

    // =======================================
    // ============= 序列化 数据 读写 ===============
    // =======================================

    /**
     * 保存 Serializable数据到 缓存中
     *
     * @param key
     * 保存的key
     * @param value
     * 保存的value
     * @param saveTime
     * 保存的时间，单位：秒
     */
    @JvmOverloads
    fun put(key: String, value: Serializable, saveTime: Int = -1) {

        val baos: ByteArrayOutputStream?

        var oos: ObjectOutputStream? = null

        try {

            baos = ByteArrayOutputStream()

            oos = ObjectOutputStream(baos)

            oos.writeObject(value)

            val data = baos.toByteArray()

            if (saveTime != -1) {
                onBytePutCache(key, data, saveTime)
            } else {
                onBytePutCache(key, data)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                oos!!.close()
            } catch (e: IOException) {
            }

        }
    }

    /**
     * 读取 Serializable数据
     *
     * @param key
     * @return Serializable 数据
     */
    fun getAsObject(key: String): Any? {
        val data = getAsBinary(key)
        if (data != null) {
            var bais: ByteArrayInputStream? = null
            var ois: ObjectInputStream? = null
            try {
                bais = ByteArrayInputStream(data)
                ois = ObjectInputStream(bais)
                return ois.readObject()
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            } finally {
                try {
                    bais?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                try {
                    ois?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return null

    }

    // =======================================
    // ============== bitmap 数据 读写 =============
    // =======================================
    /**
     * 保存 bitmap 到 缓存中
     *
     * @param key
     * 保存的key
     * @param value
     * 保存的bitmap数据
     */
    fun onBitmapPutCache(key: String, value: Bitmap?) {
        Utils.Bitmap2Bytes(value)?.let { onBytePutCache(key, it) }
    }

    /**
     * 保存 bitmap 到 缓存中
     *
     * @param key
     * 保存的key
     * @param value
     * 保存的 bitmap 数据
     * @param saveTime
     * 保存的时间，单位：秒
     */
    fun onBitmapPutCache(key: String, value: Bitmap?, saveTime: Int) {
        Utils.Bitmap2Bytes(value)?.let { onBytePutCache(key, it) }
    }

    /**
     * 读取 bitmap 数据
     *
     * @param key
     * @return bitmap 数据
     */
    fun getAsBitmap(key: String): Bitmap? {
        return if (getAsBinary(key) == null) {
            null
        } else Utils.Bytes2Bimap(getAsBinary(key)!!)
    }

    // =======================================
    // ============= drawable 数据 读写 =============
    // =======================================
    /**
     * 保存 drawable 到 缓存中
     *
     * @param key
     * 保存的key
     * @param value
     * 保存的drawable数据
     */
    fun onDrawablePutCache(key: String, value: Drawable) {
        onBitmapPutCache(key, Utils.drawable2Bitmap(value))
    }

    /**
     * 保存 drawable 到 缓存中
     *
     * @param key
     * 保存的key
     * @param value
     * 保存的 drawable 数据
     * @param saveTime
     * 保存的时间，单位：秒
     */
    fun onDrawablePutCache(key: String, value: Drawable, saveTime: Int) {
        onBitmapPutCache(key, Utils.drawable2Bitmap(value), saveTime)
    }

    /**
     * 读取 Drawable 数据
     *
     * @param key
     * @return Drawable 数据
     */
    fun getAsDrawable(key: String, context: Context): Drawable? {
        return if (getAsBinary(key) == null) {
            null
        } else Utils.bitmap2Drawable(Utils.Bytes2Bimap(getAsBinary(key)!!), context)
    }

    /**
     * 获取缓存文件
     *
     * @param key
     * @return value 缓存的文件
     */
    fun file(key: String): File? {
        val f = mCache.newFile(key)
        return if (f.exists()) f else null
    }

    /**
     * 移除某个key
     *
     * @param key
     * @return 是否移除成功
     */
    fun remove(key: String): Boolean {
        return mCache.remove(key)
    }

    /**
     * 清除所有数据
     */
    fun clear() {
        mCache.clear()
    }

    /**
     * @title 缓存管理器
     * @author 杨福海（michael） www.yangfuhai.com
     * @version 1.0
     */
    inner class ACacheManager internal constructor(
        protected var cacheDir: File,
        private val sizeLimit: Long,
        private val countLimit: Int
    ) {
        private val cacheSize: AtomicLong = AtomicLong()
        private val cacheCount: AtomicInteger = AtomicInteger()
        private val lastUsageDates = Collections.synchronizedMap(HashMap<File, Long>())

        init {
            calculateCacheSizeAndCacheCount()
        }

        /**
         * 计算 cacheSize和cacheCount
         */
        private fun calculateCacheSizeAndCacheCount() {
            Thread(Runnable {
                var size = 0
                var count = 0
                val cachedFiles = cacheDir.listFiles()
                if (cachedFiles != null) {
                    for (cachedFile in cachedFiles) {
                        size += calculateSize(cachedFile).toInt()
                        count += 1
                        lastUsageDates[cachedFile] = cachedFile.lastModified()
                    }
                    cacheSize.set(size.toLong())
                    cacheCount.set(count)
                }
            }).start()
        }

        internal fun put(file: File) {
            var curCacheCount = cacheCount.get()
            while (curCacheCount + 1 > countLimit) {
                val freedSize = removeNext()
                cacheSize.addAndGet(-freedSize)

                curCacheCount = cacheCount.addAndGet(-1)
            }
            cacheCount.addAndGet(1)

            val valueSize = calculateSize(file)
            var curCacheSize = cacheSize.get()
            while (curCacheSize + valueSize > sizeLimit) {
                val freedSize = removeNext()
                curCacheSize = cacheSize.addAndGet(-freedSize)
            }
            cacheSize.addAndGet(valueSize)

            val currentTime = System.currentTimeMillis()
            file.setLastModified(currentTime)
            lastUsageDates[file] = currentTime
        }

        internal operator fun get(key: String): File {
            val file = newFile(key)
            val currentTime = System.currentTimeMillis()
            file.setLastModified(currentTime)
            lastUsageDates[file] = currentTime

            return file
        }

        internal fun newFile(key: String): File {
            return File(cacheDir, key.hashCode().toString() + "")
        }

        internal fun remove(key: String): Boolean {
            val image = get(key)
            return image.delete()
        }

        internal fun clear() {
            lastUsageDates.clear()
            cacheSize.set(0)
            val files = cacheDir.listFiles()
            if (files != null) {
                for (f in files) {
                    f.delete()
                }
            }
        }

        /**
         * 移除旧的文件
         *
         * @return
         */
        private fun removeNext(): Long {
            if (lastUsageDates.isEmpty()) {
                return 0
            }
            var oldestUsage: Long = -1
            var mostLongUsedFile: File? = null
            synchronized(lastUsageDates) {
                val entries = lastUsageDates.entries
                entries.forEach {
                    if (mostLongUsedFile == null) {
                        mostLongUsedFile = it.key
                        oldestUsage = it.value
                    } else {

                        if (it.value < oldestUsage) {
                            oldestUsage = it.value
                            mostLongUsedFile = it.key
                        }

                    }
                }

//                for ((key, lastValueUsage) in entries) {
//                    if (mostLongUsedFile == null) {
//                        mostLongUsedFile = key
//                        oldestUsage = lastValueUsage
//                    } else {
//                        if (lastValueUsage < oldestUsage) {
//                            oldestUsage = lastValueUsage
//                            mostLongUsedFile = key
//                        }
//                    }
//                }

            }

            val fileSize = calculateSize(mostLongUsedFile!!)
            if (mostLongUsedFile!!.delete()) {
                lastUsageDates.remove(mostLongUsedFile)
            }
            return fileSize
        }

        private fun calculateSize(file: File): Long {
            return file.length()
        }
    }

    /**
     * @title 时间计算工具类
     * @author 杨福海（michael） www.yangfuhai.com
     * @version 1.0
     */
    private object Utils {

        private val mSeparator = ' '

        /**
         * 判断缓存的String数据是否到期
         *
         * @param str
         * @return true：到期了 false：还没有到期
         */
        internal fun isDue(str: String): Boolean {
            return isDue(str.toByteArray())
        }

        /**
         * 判断缓存的byte数据是否到期
         *
         * @param data
         * @return true：到期了 false：还没有到期
         */
        internal fun isDue(data: ByteArray): Boolean {
            val strs = getDateInfoFromDate(data)
            if (strs != null && strs.size == 2) {
                var saveTimeStr = strs[0]
                while (saveTimeStr.startsWith("0")) {
                    saveTimeStr = saveTimeStr.substring(1, saveTimeStr.length)
                }
                val saveTime = java.lang.Long.valueOf(saveTimeStr)
                val deleteAfter = java.lang.Long.valueOf(strs[1])
                if (System.currentTimeMillis() > saveTime + deleteAfter * 1000) {
                    return true
                }
            }
            return false
        }

        internal fun newStringWithDateInfo(second: Int, strInfo: String): String {
            return createDateInfo(second) + strInfo
        }

        internal fun newByteArrayWithDateInfo(second: Int, data2: ByteArray): ByteArray {
            val data1 = createDateInfo(second).toByteArray()
            val retdata = ByteArray(data1.size + data2.size)
            System.arraycopy(data1, 0, retdata, 0, data1.size)
            System.arraycopy(data2, 0, retdata, data1.size, data2.size)
            return retdata
        }

        internal fun clearDateInfo(strInfo: String?): String? {

            if (hasDateInfo(strInfo?.toByteArray())) return null

            return strInfo?.let {

                val num = strInfo.indexOf(mSeparator).plus(1)

                it.substring(num, it.length)
            }
        }

        internal fun clearDateInfo(data: ByteArray): ByteArray {
            return if (hasDateInfo(data)) {
                copyOfRange(data, indexOf(data, mSeparator) + 1, data.size)
            } else data
        }

        private fun hasDateInfo(data: ByteArray?): Boolean {
            return data != null && data.size > 15 && data[13] == '-'.toByte() && indexOf(data, mSeparator) > 14
        }

        private fun getDateInfoFromDate(data: ByteArray): Array<String>? {
            if (hasDateInfo(data)) {
                val saveDate = String(copyOfRange(data, 0, 13))
                val deleteAfter = String(copyOfRange(data, 14, indexOf(data, mSeparator)))
                return arrayOf(saveDate, deleteAfter)
            }
            return null
        }

        private fun indexOf(data: ByteArray, c: Char): Int {
            for (i in data.indices) {
                if (data[i] == c.toByte()) {
                    return i
                }
            }
            return -1
        }

        private fun copyOfRange(original: ByteArray, from: Int, to: Int): ByteArray {
            val newLength = to - from
            if (newLength < 0)
                throw IllegalArgumentException("$from > $to")
            val copy = ByteArray(newLength)
            System.arraycopy(original, from, copy, 0, Math.min(original.size - from, newLength))
            return copy
        }

        private fun createDateInfo(second: Int): String {
            val currentTime = StringBuilder(System.currentTimeMillis().toString() + "")
            while (currentTime.length < 13) {
                currentTime.insert(0, "0")
            }
            return "$currentTime-$second$mSeparator"
        }

        /*
		 * Bitmap → byte[]
		 */
        internal fun Bitmap2Bytes(bm: Bitmap?): ByteArray? {
            if (bm == null) {
                return null
            }
            val baos = ByteArrayOutputStream()
            bm.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos)
            return baos.toByteArray()
        }

        /*
		 * byte[] → Bitmap
		 */
        internal fun Bytes2Bimap(b: ByteArray): Bitmap? {
            return if (b.isEmpty()) {
                null
            } else BitmapFactory.decodeByteArray(b, 0, b.size)
        }

        /*
		 * Drawable → Bitmap
		 */
        internal fun drawable2Bitmap(drawable: Drawable?): Bitmap? {

            if (drawable == null) {
                return null
            }

            // 取 drawable 的长宽
            val w = drawable.intrinsicWidth
            val h = drawable.intrinsicHeight

            // 取 drawable 的颜色格式
            val config =
                if (drawable.opacity != PixelFormat.OPAQUE) android.graphics.Bitmap.Config.ARGB_8888 else android.graphics.Bitmap.Config.RGB_565

            // 建立对应 bitmap
            val bitmap = android.graphics.Bitmap.createBitmap(w, h, config)

            return bitmap?.apply {
                // 建立对应 bitmap 的画布
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, w, h)
                // 把 drawable 内容画到画布中
                drawable.draw(canvas)
            }
        }

        /*
		 * Bitmap → Drawable
		 */
        internal fun bitmap2Drawable(bm: Bitmap?, context: Context): Drawable? {

            if (bm == null) {
                return null
            }

            val bd = BitmapDrawable(context.resources, bm)

            bd.setTargetDensity(bm.density)

            return BitmapDrawable(context.resources, bm)
        }

    }

    companion object {

        const val STRING = "String"
        const val Binary = "Binary"
        const val Bitmap = "Bitmap"
        const val Drawable = "Drawable"
        const val Object = "Object"
        const val jsonArray = "jsonArray"
        const val jsonObject = "jsonObject"

        const val TIME_HOUR = 60 * 60
        const val TIME_DAY = TIME_HOUR * 24

        private const val MAX_SIZE = 1000 * 1000 * 50 // 50 mb
        private const val MAX_COUNT = Integer.MAX_VALUE // 不限制存放数据的数量
        private val mInstanceMap = HashMap<String, ACache>()

        @JvmOverloads
        operator fun get(ctx: Context, cacheName: String = "ACache"): ACache {
            val f = File(ctx.cacheDir, cacheName)
            return get(f, MAX_SIZE.toLong(), MAX_COUNT)
        }

        operator fun get(ctx: Context, max_zise: Long, max_count: Int): ACache {
            val f = File(ctx.cacheDir, "ACache")
            return get(f, max_zise, max_count)
        }

        @JvmOverloads
        operator fun get(cacheDir: File, max_zise: Long = MAX_SIZE.toLong(), max_count: Int = MAX_COUNT): ACache {
            var manager = mInstanceMap[cacheDir.absoluteFile.toString() + myPid()]
            if (manager == null) {
                manager = ACache(cacheDir, max_zise, max_count)
                mInstanceMap[cacheDir.absolutePath + myPid()] = manager
            }
            return manager
        }

        private fun myPid(): String {
            return "_" + android.os.Process.myPid()
        }

    }

}