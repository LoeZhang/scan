package com.loe.test

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.king.zxing.util.CodeUtils
import com.loe.scan.LoeScan
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonJump.setOnClickListener()
        {
            LoeScan.scan(this)
            {
                textView.text = it
            }
        }

        createQRCode("http://www.baidu.com")
        createBarCode("123456")
    }

    /**
     * 生成二维码
     */
    private fun createQRCode(content: String)
    {
        //生成二维码最好放子线程生成防止阻塞UI，这里只是演示
        val logo = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_background)
        val bitmap = CodeUtils.createQRCode(content, 600, logo)
        //显示二维码
        imageQR.setImageBitmap(bitmap)
    }

    /**
     * 生成条形码
     */
    private fun createBarCode(content: String)
    {
        //生成条形码最好放子线程生成防止阻塞UI，这里只是演示
        val bitmap = CodeUtils.createBarCode(content, BarcodeFormat.CODE_128, 800, 200, null, true)
        //显示条形码
        imageBar.setImageBitmap(bitmap)
    }
}