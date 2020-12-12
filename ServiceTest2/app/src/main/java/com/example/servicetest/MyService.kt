package com.example.servicetest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import kotlin.concurrent.thread

class MyService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)

    }

    private val mBinder = DownloadBinder()

    inner class DownloadBinder : Binder() {

        val TYPE_SUCCESS: Int = 0
        val TYPE_FAILED: Int = 1
        val TYPE_PAUSED: Int = 2
        val TYPE_CANCELED: Int = 3
        val isCanceled: Boolean = false
        val isPaused: Boolean = false


        var a: Int = -1


        // var progressforactivity:Int=-1
        fun startDownload(){

            //var b: Int = -1
            //Log.d("MyService", "startDownload: ")
            val handler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    a = msg.what
                    when(a){
                        TYPE_SUCCESS->{onSuccess()}
                        TYPE_FAILED->{onFailed()}
                       TYPE_PAUSED->{onPaused()}
                       TYPE_CANCELED->{onCanceled()}
                    }
                    //b = msg.arg1
                }
            }
            thread {
                val msg = Message()
                var inputStream: InputStream? = null
                var randomAccessFile: RandomAccessFile? = null
                var file: File? = null
                try {
                    var downloadedLength: Long = 0
                    var compareLength: Long = 0
                    var progress:Int=0

                    val downloadUrl: String ="https://www.st.com/resource/en/datasheet/stm32f401re.pdf"
                    
                        //"https://www.baidu.com/img/bd_logo1.png"
                        //"https://www.st.com/resource/en/datasheet/stm32f401re.pdf"
                    val fileName: String = downloadUrl.substring(downloadUrl.lastIndexOf("/"))
                    val directory: String =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                    file = File(directory + fileName)
                    if (file.exists()) {
                        downloadedLength = file.length()
                        Log.d("Downloadtest", "downloadedLength is $downloadedLength")
                    }

                    var contentLength: Long = getContentLength(downloadUrl)
                    Log.d("Downloadtest", "contentLength is $contentLength")
                    Log.d("Downloadtest", "contentLength is ${contentLength.toInt()}")
                   // Log.d("Downloadtest", "contentLength is $contentLength")
                    if (contentLength == compareLength) {
                        msg.what = TYPE_FAILED
                    } else if (contentLength == downloadedLength) {
                        msg.what = TYPE_SUCCESS
                    }
                    val client = OkHttpClient()
                    val request =
                        Request.Builder().addHeader("RANGE", "bytes=" + downloadedLength + "-")
                        //Request.Builder()
                            // 指定访问的服务器地址是电脑本机
//                    .url("http://10.0.2.2/get_data.xml")
                            .url(downloadUrl)
                            .build()
                    val response = client.newCall(request).execute()
                    if (response != null) {

                        inputStream = response.body!!.byteStream()
                        randomAccessFile = RandomAccessFile(file, "rw")
                        //randomAccessFile.seek(downloadedLength)
                        val b: ByteArray = ByteArray(1024)
                        var total: Int = downloadedLength.toInt()
                        var len: Int = inputStream.read(b)
                        var progresscount=0
                        while (len!= -1 && progresscount!=1){//(total + downloadedLength.toInt())!=(contentLength.toInt()))

                            if (isCanceled) {
                                msg.what = TYPE_CANCELED
                            } else if (isPaused) {
                                msg.what = TYPE_PAUSED
                            } else {

                                if(len+total>contentLength){
                                    len=contentLength.toInt()-total
                                    Log.d("Downloadtest","len is $len")
                                    randomAccessFile.write(b,0,len)
                                    total+=len


                                }else if(len+total<contentLength){
                                    randomAccessFile.write(b,0,len)

                                    total+=len
                                    len=inputStream.read(b)
                                }else{
                                    randomAccessFile.write(b,0,len)
                                    total+=len

                                }
//                                if(((contentLength.toInt())-(total + downloadedLength.toInt()))>=1024){total += len
//                                    randomAccessFile.write(b, 0, len)
//                                    progress = (total + downloadedLength.toInt())*100/(contentLength.toInt())}
//                                else if(((contentLength.toInt())-(total + downloadedLength.toInt()))>=100){ val c: ByteArray = ByteArray(100)
//                                    var len: Int = inputStream.read(c)
//                                    total += len
//                                    randomAccessFile.write(c, 0, len)
//                                    progress = (total + downloadedLength.toInt())*100/(contentLength.toInt())
//
//                                }else if (((contentLength.toInt())-(total + downloadedLength.toInt()))>=10){
//                                    val d: ByteArray = ByteArray(10)
//                                    var len: Int = inputStream.read(d)
//                                    total += len
//                                    randomAccessFile.write(d, 0, len)
//                                    progress = (total + downloadedLength.toInt())*100/(contentLength.toInt())
//                                }else{
//                                    val e: ByteArray = ByteArray(1)
//                                    var len: Int = inputStream.read(e)
//                                    total += len
//                                    randomAccessFile.write(e, 0, len)
//                                    progress = (total + downloadedLength.toInt())*100/(contentLength.toInt())
//                                }
                                progress = total*100/(contentLength.toInt())

                                if ((progress%10)==0&& progress!=0){getProgress(progress)
                                if (progress==100){

                                    progresscount=1
                                    Log.d("Downloadtest", "progresscount is $progresscount")}}



                                //progressforactivity=progress

                            }
                        }
                        response.body!!.close()
                        msg.what = TYPE_SUCCESS
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close()
                        }
                        if (randomAccessFile != null) {
                            randomAccessFile.close()
                        }
                        if (isCanceled && file != null) {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                handler.sendMessage(msg)
            }


        }



        private fun getContentLength(downloadUrl: String): Long {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().addHeader("Accept-Encoding", "identity")
                    // 指定访问的服务器地址是电脑本机
//                    .url("http://10.0.2.2/get_data.xml")
                    .url(downloadUrl)
                    .build()
                val response = client.newCall(request).execute()
                if (response != null && response.isSuccessful) {
                    val contentLength: Long = response.body!!.contentLength()
                    response.body!!.close()
                    return contentLength
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return 0
        }
    }
    fun getProgress( progress:Int) {
        //return progress
        val  manager:NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("myservice", "myservice", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)}
        
            val notification = NotificationCompat.Builder(this, "myservice")
                .setContentTitle("Downloading......")
                .setContentText("$progress%")
//                .setStyle(NotificationCompat.BigTextStyle().bigText("Learn how to build notifications, send and sync data, and use voice actions. Get the official Android IDE and developer tools to build apps for Android."))
//                .setStyle(NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(resources, R.drawable.big_image)))
                .setSmallIcon(R.drawable.small_icon)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.large_icon
                    )
                )
                .setAutoCancel(true)
                .build()

when(progress){
    10->manager.notify(1, notification)
    20->{ manager.cancel(1)
        manager.notify(2, notification)
   }
    30->{manager.cancel(2)
        manager.notify(3, notification) }
    40->{manager.cancel(3)
        manager.notify(4, notification)}
    50->{manager.cancel(4)
        manager.notify(5, notification)}
    60->{manager.cancel(5)
        manager.notify(6, notification)}
    70->{manager.cancel(6)
        manager.notify(7, notification)}
    80->{manager.cancel(7)
        manager.notify(8, notification)}
    90->{manager.cancel(8)
        manager.notify(9, notification)}
    100->{manager.cancel(9)
        manager.notify(10, notification)}
}





    }
    fun onSuccess() {
        //return progress
        val  manager:NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("myservice", "myservice", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)}

        val notification1 = NotificationCompat.Builder(this, "myservice")
            .setContentTitle("Download Success")
            .setContentText("Download Success")
//                .setStyle(NotificationCompat.BigTextStyle().bigText("Learn how to build notifications, send and sync data, and use voice actions. Get the official Android IDE and developer tools to build apps for Android."))
//                .setStyle(NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(resources, R.drawable.big_image)))
            .setSmallIcon(R.drawable.small_icon)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.large_icon
                )
            )
            .setAutoCancel(true)
            .build()

        manager.notify(11, notification1)




    }
    fun onFailed() {
        //return progress
        val  manager:NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("myservice", "myservice", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)}

        val notification2 = NotificationCompat.Builder(this, "myservice")
            .setContentTitle("Download Failed")
            .setContentText("Download Failed")
//                .setStyle(NotificationCompat.BigTextStyle().bigText("Learn how to build notifications, send and sync data, and use voice actions. Get the official Android IDE and developer tools to build apps for Android."))
//                .setStyle(NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(resources, R.drawable.big_image)))
            .setSmallIcon(R.drawable.small_icon)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.large_icon
                )
            )
            .setAutoCancel(true)
            .build()

        manager.notify(12, notification2)




    }
    fun onPaused() {
        //return progress
        Toast.makeText(this,"Paused", Toast.LENGTH_SHORT).show()
    }
    fun onCanceled() {
        //return progress
        Toast.makeText(this,"Canceled", Toast.LENGTH_SHORT).show()
    }
        override fun onBind(intent: Intent): IBinder {
            return mBinder
        }

}