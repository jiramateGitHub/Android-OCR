package com.example.orcapplication

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.orcapplication.Constants.INITIAL_PERMS
import com.googlecode.tesseract.android.TessBaseAPI
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_ocr.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File


class MainActivity : AppCompatActivity() {

    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, INITIAL_PERMS, Constants.INITIAL_REQUEST)

        btnTakePhoto.setOnClickListener {
            EasyImage.openCameraForImage(this, 0)
        }

        btnGallery.setOnClickListener {
            EasyImage.openGallery(this, 0)
        }

        btnCopy.setOnClickListener{
            val textToCopy = tvResult.text
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", textToCopy)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
        }

//        val manager = supportFragmentManager
//        val transaction = manager.beginTransaction()
//        transaction.replace(R.id.contentContainer, Ocr(), "fragment_ocr")
//        transaction.addToBackStack("fragment_ocr")
//        transaction.commit()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onImagesPicked(imageFiles: MutableList<File>, source: EasyImage.ImageSource?, type: Int) {
                CropImage.activity(Uri.fromFile(imageFiles[0])).start(this@MainActivity)
            }
        })
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val result = CropImage.getActivityResult(data)
                imageFile = File(result.uri.path)
                loadImage(imageFile)
                ConvertTask().execute(imageFile)
            }
        }
    }

    private fun loadImage(imageFile: File?) {
        Glide.with(this)
            .load(imageFile)
            .into(ivOCR)
    }

    private inner class ConvertTask : AsyncTask<File, Void, String>() {
        internal var tesseract = TessBaseAPI()

        override fun onPreExecute() {
            super.onPreExecute()
            val datapath = "$filesDir/tesseract/";
            FileUtil.checkFile(
                this@MainActivity,
                datapath.toString(),
                File(datapath + "tessdata/")
            )
            tesseract.init(datapath, "eng")
            tvResult.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg files: File): String {
            val options = BitmapFactory.Options()
            options.inSampleSize =
                4 // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            val bitmap = BitmapFactory.decodeFile(imageFile?.path, options)
            tesseract.setImage(bitmap)
            val result = tesseract.utF8Text
            tesseract.end()
            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            tvResult.setText(result)
            tvResult.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }

}