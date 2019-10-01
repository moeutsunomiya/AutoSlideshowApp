package com.example.autoslideshowapp

import android.Manifest
import android.app.AlertDialog
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    //パーミッションコード
    private val PERMISSIONS_REQUEST_CODE = 100

    //URI検索結果格納用の配列
    val list: MutableList<Uri> = mutableListOf()

    //URI検索結果格納用の配列の要素を示す変数
    var listNum = 0

    //「再生/停止」ボタン用の変数
    var mTimer: Timer? = null
    var mHandler = Handler()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
                //画像初期表示
                imageView.setImageURI(list[0])

                //「進む」ボタン押下時
                next_button.setOnClickListener {
                    setNext_button()
                }

                //「戻る」ボタン押下時
                reverse_button.setOnClickListener {
                    setReverse_button()
                }

                //「再生」ボタン押下時
                restart_button.setOnClickListener {
                    strSlideShow()
                }

            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    //パーミッションが許可されていなかったときの処理
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("ANDROID","許可")
                }else{
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setTitle("注意")
                    alertDialogBuilder.setMessage("パーミッションが拒否されました。")
                    alertDialogBuilder.setPositiveButton("OK",null)
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                    Log.d("ANDROID","許可されなかった")
                }
        }
    }


    //画像URIを取得する
    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    // indexからIDを取得し、そのIDから画像のURIを取得する
                    val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val id = cursor.getLong(fieldIndex)
                    val imageUri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    Log.d("ANDROID", "URI : " + imageUri.toString())
                    //imageView.setImageURI(imageUri)

                    //検索結果を配列に格納
                    list.add(imageUri)

                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        //検索結果がリストに追加されたか確認
        Log.d("LIST", list.toString())
    }


    //「進む」ボタンが押されたときの処理
    private fun setNext_button() {

        //画像が最後の画像の場合、最初の画像を表示する
        if (listNum == list.size - 1) {
            listNum = 0
            imageView.setImageURI(list[listNum])
        }else{
            listNum++
            imageView.setImageURI(list[listNum])
        }
    }

    //「戻る」ボタンが押されたときの処理
    private fun setReverse_button() {
        imageView.setImageURI(list[list.size - 1])

        //画像が最初の画像の場合、iに最後の画像を設定する
        if (listNum == 0) {
            listNum = list.size - 1
            imageView.setImageURI(list[listNum])

        } else {
            listNum--
            imageView.setImageURI(list[listNum])
        }
    }

    //「再生」ボタンが押されたときの処理
    private fun strSlideShow() {

        if (restart_button.text == "再生") {
            next_button.setEnabled(false) //「進む」ボタンタップ不可
            reverse_button.setEnabled(false)//「戻る」ボタンタップ不可
            restart_button.text = "停止"

            //「再生」ボタンが押された場合の処理
            //タイマーの始動
            if (mTimer == null) {

                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {

                    override fun run() {
                        mHandler.post {

                            //写真が最後の場合
                            if (list[listNum] == list[4]) {
                                listNum = -1 //iの初期化を行う
                            }
                            listNum++

                            //スライドショーを切り替える処理
                            imageView.setImageURI(list[listNum])
                        }
                    }
                }, 2000, 2000)
            }

        }else if(restart_button.text == "停止"){
            next_button.setEnabled(true) //「進む」ボタンタップ可
            reverse_button.setEnabled(true)//「戻る」ボタンタップ可
            restart_button.text = "再生"

            if(mTimer != null) {
                mTimer!!.cancel()
                mTimer = null
            }
        }

    }
}