package com.example.servicetest

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
  lateinit var manager:NotificationManager



    lateinit var downloadBinder: MyService.DownloadBinder
    private val connection=object:ServiceConnection{
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
           downloadBinder=service as MyService.DownloadBinder
            downloadBinder.startDownload()
//            when(downloadBinder.startDownload()){
//                downloadBinder.TYPE_SUCCESS->{onSuccess()}
//                downloadBinder.TYPE_FAILED->{onFailed()}
//                downloadBinder.TYPE_PAUSED->{onPaused()}
//                downloadBinder.TYPE_CANCELED->{onCanceled()}
//            }
           // downloadBinder.getProgress()
        }

        override fun onServiceDisconnected(name: ComponentName) {

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("myservice", "myservice", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)}

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),2)
        }
        startDownloadBtn.setOnClickListener {
            val intent=Intent(this,MyService::class.java)
            bindService(intent,connection, Context.BIND_AUTO_CREATE)
        }
       pauseDownloadBtn.setOnClickListener {
            unbindService(connection)
        }
        cancelDownloadBtn.setOnClickListener {

        }
    }

    fun onProgress(progress: Int){
        if (progress>=0){
            val notification = NotificationCompat.Builder(this, "Normal")
                .setContentTitle("Downloading......")
                .setContentText("$progress%")
//                .setStyle(NotificationCompat.BigTextStyle().bigText("Learn how to build notifications, send and sync data, and use voice actions. Get the official Android IDE and developer tools to build apps for Android."))
//                .setStyle(NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(resources, R.drawable.big_image)))
                .setSmallIcon(R.drawable.small_icon)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.large_icon))
                .setAutoCancel(true)
                .build()
            manager.notify(1, notification)

        }
    }
//    fun onSuccess(){
//        val notification = NotificationCompat.Builder(this, "myservice")
//            .setContentTitle("Download Success")
//            .setContentText("Download Success")
////                .setStyle(NotificationCompat.BigTextStyle().bigText("Learn how to build notifications, send and sync data, and use voice actions. Get the official Android IDE and developer tools to build apps for Android."))
////                .setStyle(NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(resources, R.drawable.big_image)))
//            .setSmallIcon(R.drawable.small_icon)
//            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.large_icon))
//            .setAutoCancel(true)
//            .build()
//        manager.notify(1, notification)
//    }
//    fun onFailed(){
//        val notification = NotificationCompat.Builder(this, "myservice")
//            .setContentTitle("Download Failed")
//            .setContentText("Download Failed")
////                .setStyle(NotificationCompat.BigTextStyle().bigText("Learn how to build notifications, send and sync data, and use voice actions. Get the official Android IDE and developer tools to build apps for Android."))
////                .setStyle(NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(resources, R.drawable.big_image)))
//            .setSmallIcon(R.drawable.small_icon)
//            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.large_icon))
//            .setAutoCancel(true)
//            .build()
//        manager.notify(1, notification)
//    }
//    fun onPaused(){
//        Toast.makeText(this,"Paused",Toast.LENGTH_SHORT).show()
//    }
//    fun onCanceled(){
//        Toast.makeText(this,"Canceled",Toast.LENGTH_SHORT).show()
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1->{if (grantResults.isNotEmpty()&&grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"拒绝写文件权限将无法使用程序！",Toast.LENGTH_SHORT).show()

            }}
            2->{if (grantResults.isNotEmpty()&&grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"拒绝读文件权限将无法使用程序！",Toast.LENGTH_SHORT).show()

            }}
        }
    }
}
