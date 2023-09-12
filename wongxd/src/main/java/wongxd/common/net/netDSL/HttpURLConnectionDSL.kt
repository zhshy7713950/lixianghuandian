package wongxd.common.net.netDSL

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import org.json.JSONObject
import wongxd.common.EasyToast
import wongxd.utils.utilcode.util.LogUtils
import java.io.*
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


/**
 * HttpUrlConnection 的 dsl
 *
 * Created by wongxd on 2018/11/20.
 * https://github.com/wongxd
 * wxd1@live.com
 */


object BaseHttpUrlConnectionHelper {


    internal class BaseHttpUrlConnectionHelperLooper protected constructor(looper: Looper) : Handler(looper) {

        companion object {
            val instance =
                BaseHttpUrlConnectionHelperLooper(Looper.getMainLooper())

            fun runOnUiThread(block: () -> Unit) {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    Runnable { block.invoke() }.run()
                } else {
                    instance.post { block.invoke() }
                }

            }
        }
    }


    /**
     * 通用的http请求封装，不包含任何个性化参数
     */
    fun baseHttpUrlConnection(wrap: RequestWrapper, isDownlod: Boolean = false) {
        Thread(Runnable {

            if (isDownlod)
                downloadFile(wrap)
            else
                onExecute(wrap)

        }).start()
    }


    val end = "\r\n"
    val twoHyphens = "--"
    val boundary = "*****"


    private fun encode(value: String): String {
        return URLEncoder.encode(value, "UTF-8")
    }

    private fun packOneParams(
        dataOutputStream: DataOutputStream,
        paramName: String,
        paramValue: String
    ) {
        dataOutputStream.writeBytes(twoHyphens + boundary + end)
        dataOutputStream.writeBytes(
            "Content-Disposition: form-data; name=\""
                    + encode(paramName) + "\""
        )
        dataOutputStream.writeBytes(end)
        dataOutputStream.writeBytes("Content-Type:text/plain")
        dataOutputStream.writeBytes(end)
        dataOutputStream.writeBytes(end)
        dataOutputStream.writeBytes(encode(paramValue) + end)
        dataOutputStream.writeBytes(twoHyphens + boundary + end)
    }


    /**
     * @param totalLength  所有文件的总长度
     * @param index 当前文件在所有上传文件中的位置
     */
    private fun packOneFile(
        isImg: Boolean,
        file: MutableMap.MutableEntry<String, File>,
        dataOutputStream: DataOutputStream,
        //progress  totalLength index
        onProgressUpdate: (Int, Long, Int) -> Unit,
        totalLength: Long,
        index: Int
    ) {
        val uploadImg = file.value
        val filename = file.value.name
        val extension = file.value.extension

        dataOutputStream.writeBytes(twoHyphens + boundary + end)
        // 设定传送的内容类型是可序列化的java对象
        // (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
        dataOutputStream.writeBytes(
            "Content-Disposition: form-data; " + "name=\"" + file.key + "\";filename=\"" + filename
                    + "\"" + end
        )
        if (isImg) {
            dataOutputStream.writeBytes("Content-Type: image/$extension")
        } else {
            dataOutputStream.writeBytes("Content-Type: file/$extension")
        }

        dataOutputStream.writeBytes(end)
        dataOutputStream.writeBytes("Content-Lenght: " + uploadImg.length().toString())
        dataOutputStream.writeBytes(end)
        dataOutputStream.writeBytes(end)

        val dataInputStream = DataInputStream(FileInputStream(uploadImg))
        var count = 0

        // 计算上传进度
        var progress: Long = 0L

        val buffer = ByteArray(2 * 1024)

        while (dataInputStream.read(buffer).also { count = it } != -1) {
            dataOutputStream.write(buffer, 0, count)

            progress += count.toLong()
            //换算进度

            val d =
                BigDecimal(progress / uploadImg.length().toDouble()).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()

            val d1 = d * 100

            BaseHttpUrlConnectionHelperLooper.runOnUiThread {
                //d1.toInt() 的值为1-100
                onProgressUpdate(d1.toInt(), totalLength, index)
            }

        }
        dataOutputStream.writeBytes(end)
        /* close streams */
        dataInputStream.close()
    }

    private fun onExecute(wrap: RequestWrapper) {

        val isPost = wrap.method.toUpperCase().contains("POST")

        val isMutipart = wrap.imgs.isNotEmpty() || wrap.files.isNotEmpty()

        val isPostJson = wrap.jsonParam.size > 4

        if (isMutipart || isPostJson) {
            if (!isPost) {
                throw IllegalArgumentException("必须使用post方法")
            }
        }

        try {

            if (!isPost) {
                //get请求带参数
                val sbGet = StringBuffer()
                sbGet.append("?")
                wrap.params.forEach { param ->
                    sbGet.append(param.key + "=" + param.value + "&")
                }

                val getParams = sbGet.toString()

                wrap.url = wrap.url + getParams.substring(0, getParams.lastIndex)
            }


            // 统一资源
            val url: URL = URL(wrap.url)
            // 连接类的父类，抽象类 as  http的连接类
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            // 设定请求的方法，默认是GET
            connection.requestMethod = if (isPost) "POST" else "GET"
            // 设置字符编码连接参数
            connection.addRequestProperty("Connection", "Keep-Alive")
            // 设置字符编码
            connection.addRequestProperty("Charset", "UTF-8")
            // 设置请求内容类型

            if (isPostJson) {
                connection.addRequestProperty("Content-Type", "application/json;charset=utf-8")
            } else if (isMutipart) {
                connection.addRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
            } else {
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }


            // 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在
            // http正文内，因此需要设为true, 默认情况下是false;
            connection.doOutput = isPost
            //设置是否从httpUrlConnection读入，默认情况下是true;
            connection.doInput = true
            // Post 请求不能使用缓存
            connection.useCaches = false
            connection.connectTimeout = wrap.timeout.toInt()
            connection.readTimeout = wrap.readTimeout.toInt()

            wrap.headers.forEach { header ->
                connection.addRequestProperty(header.key, header.value)
            }




            if (isPost) {

//                //如果有文件需要上传，那么，需要额外设置
//                if (isMutipart) {
//                    var fileLength = 0L
//
//                    wrap.imgs.forEach { img ->
//                        fileLength += img.value.length()
//                    }
//
//                    wrap.files.forEach { file ->
//                        fileLength += file.value.length()
//                    }
//
//                    wrap.params.forEach { param ->
//                        fileLength += param.value.length
//                    }
//
//                    connection.setRequestProperty("Content-Length", fileLength.toString())
//
//                }


                //会自动开启连接 不用 再调用   connection.connect()
                val outputStream = connection.outputStream

                // 设置DataOutputStream
                val dataOutputStream: DataOutputStream = DataOutputStream(outputStream)

                if (isMutipart) {
                    //文件 参数 一起 post


                    var totalFileLength = 0L
                    wrap.imgs.forEach { img ->
                        totalFileLength += img.value.length()
                    }

                    wrap.files.forEach { file ->
                        totalFileLength += file.value.length()
                    }


                    var index = 0
                    //imgs
                    for (img in wrap.imgs) {
                        packOneFile(true, img, dataOutputStream, wrap._uploadFile, totalFileLength, index)
                        index++
                    }

                    //files
                    for (file in wrap.files) {
                        packOneFile(false, file, dataOutputStream, wrap._uploadFile, totalFileLength, index)
                        index++
                    }


                    //params
                    val sb = StringBuilder()
                    wrap.params.keys.forEach { key ->
                        packOneParams(dataOutputStream, key, wrap.params[key] ?: "")
                        sb.append(key + "--" + wrap.params[key])
                        sb.append(end)
                    }

                    LogUtils.iTag("requsest ${wrap.url}", sb.toString())



                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + end)


                    /* close streams */
                    dataOutputStream.flush()
                    dataOutputStream.close()

                } else if (isPostJson) {
                    //post json
                    LogUtils.iTag("requsest ${wrap.url}", wrap.jsonParam)

//                val writer = BufferedWriter(OutputStreamWriter(outputStream, "UTF-8"))
//                writer.write(wrap.jsonParam)
//                writer.close()

                    dataOutputStream.write(Gson().toJson(wrap.jsonParam).toByteArray())
                    dataOutputStream.flush()
                    dataOutputStream.close()

                } else {
                    //普通post
                    val sbPost = StringBuffer()
                    wrap.params.forEach { param ->
                        sbPost.append(param.key + "=" + param.value + "&")
                    }
                    val postParams = sbPost.toString()
                    val realParams = postParams.substring(0, postParams.lastIndex)

                    LogUtils.iTag("requsest ${wrap.url}", realParams)

                    dataOutputStream.write(realParams.toByteArray())
                    dataOutputStream.flush()
                    dataOutputStream.close()
                }


                outputStream.close()
            } else {
                //开启连接
                connection.connect()
            }


            //////////处理响应开始

            val inputStream = connection.inputStream

            val responseString = is2String(inputStream) ?: ""

            dealResponse(connection.responseCode, responseString, wrap)

            inputStream?.close()
            connection.disconnect()

        } catch (e: Exception) {
            e.printStackTrace()

            BaseHttpUrlConnectionHelperLooper.runOnUiThread {
                wrap._finish.invoke()

                val msg = "网络没有响应"
                if (wrap.IS_SHOW_MSG) {
                    EasyToast.DEFAULT.show(msg)
                }
                wrap._fail.invoke(-1, msg)
            }
        }


    }


    /**
     * 从response 的 inputStream 中 获取响应内容
     */
    private fun is2String(`is`: InputStream): String? {

        val buf: String

        try {

            val reader = BufferedReader(InputStreamReader(`is`, "utf-8"))

            val ssb = StringBuilder()

            var tempLine = ""

            while (reader.readLine()?.also { tempLine = it } != null) {
                ssb.append(tempLine + "\n")
            }

            `is`.close()

            buf = ssb.toString()

            return buf


        } catch (e: Exception) {

            return null

        }

    }

    /**
     * 处理响应
     */
    private fun dealResponse(responseCode: Int, responseString: String, wrap: RequestWrapper) {


        if (responseCode >= 300 || responseCode != HttpURLConnection.HTTP_OK) {

            val tips = "HTTP Request is not success, Response code is " + responseCode
            LogUtils.iTag("net", tips)

            BaseHttpUrlConnectionHelperLooper.runOnUiThread {

                wrap._finish.invoke()

                val msg = "网络没有响应"
                if (wrap.IS_SHOW_MSG) {
                    EasyToast.DEFAULT.show(msg)
                }
                wrap._fail.invoke(-1, msg)

            }


        }

        if (responseCode == HttpURLConnection.HTTP_OK) {


            BaseHttpUrlConnectionHelperLooper.runOnUiThread {
                wrap._finish.invoke()

                LogUtils.iTag("requsest", " ${wrap.url} \n", responseString)

                if (responseString.isNotBlank()) {
                    try {
                        val json = JSONObject(responseString)
                        val errcode = json.optInt(wrap.RESPONSE_CODE_NAME)
                        val errMsg = json.optString(wrap.RESPONSE_MSG_NAME)

                        if (errcode != wrap.SUCUUESSED_CODE) {
                            if (wrap.IS_SHOW_MSG) {
                                EasyToast.DEFAULT.show(errMsg)
                            }
                            if (errcode == wrap.TOKEN_LOST_CODE) {
                                //token失效
                                wrap._tokenLost.invoke(errMsg)

                            } else
                                wrap._fail.invoke(errcode, errMsg)
                        } else {
                            wrap._success.invoke(responseString)
                            wrap._successWithMsg.invoke(responseString, errMsg)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        wrap._originResponse.invoke(responseString)
                    }

                } else {
                    val msg = "网络没有响应"

                    if (wrap.IS_SHOW_MSG) {
                        EasyToast.DEFAULT.show(msg)
                    }
                    wrap._fail.invoke(-1, msg)
                }

            }
        }
    }


    private fun downloadFile(wrap: RequestWrapper) {

        if (wrap.downloadFileName.isBlank()) {
            throw  java.lang.IllegalArgumentException("请设置下载文件的保存名字")
        }

        val url = URL(wrap.url)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        try {
            val inputStream = connection.inputStream
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {

                val buf = ByteArray(1024 * 8)

                var len = 0

                var fos: FileOutputStream? = null


                val total = connection.contentLength


                var sum: Long = 0


                val dir = File(wrap.downloadPath)

                if (!dir.exists()) {
                    dir.mkdirs()
                }

                val file = File(dir, wrap.downloadFileName) //根据目录和文件名得到file对象

                fos = FileOutputStream(file)

                while ((inputStream.read(buf)).also { len = it } != -1) {

                    sum += len.toLong()

                    fos.write(buf, 0, len)


                    BaseHttpUrlConnectionHelperLooper.runOnUiThread {
                        wrap._downloadFile((sum * 1.0f / total * 100).toInt(), total.toLong(), null)
                    }

                }

                fos.flush()
                fos.close()

                BaseHttpUrlConnectionHelperLooper.runOnUiThread {
                    wrap._finish.invoke()
                    wrap._downloadFile.invoke(100, total.toLong(), file)
                }
            }

            if (responseCode >= 300 || responseCode != HttpURLConnection.HTTP_OK) {

                val tips = "HTTP Request is not success, Response code is " + responseCode
                LogUtils.iTag("net", tips)

                BaseHttpUrlConnectionHelperLooper.runOnUiThread {
                    wrap._finish.invoke()

                    val msg = "网络没有响应"
                    if (wrap.IS_SHOW_MSG) {
                        EasyToast.DEFAULT.show(msg)
                    }
                    wrap._fail.invoke(-1, msg)
                }

            }

            inputStream.close()
            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()

            BaseHttpUrlConnectionHelperLooper.runOnUiThread {
                wrap._finish.invoke()

                val msg = "网络没有响应"
                if (wrap.IS_SHOW_MSG) {
                    EasyToast.DEFAULT.show(msg)
                }
                wrap._fail.invoke(-1, msg)
            }
        }

    }


}



