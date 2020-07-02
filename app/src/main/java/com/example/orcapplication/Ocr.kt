package com.example.orcapplication

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.googlecode.tesseract.android.TessBaseAPI
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_ocr.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File


class Ocr : Fragment() {
    private var imageFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_ocr, container, false)

        val btnTakePhoto = view.findViewById(R.id.btnTakePhoto) as Button
        val btnGallery = view.findViewById(R.id.btnGallery) as Button

        btnTakePhoto.setOnClickListener {
            EasyImage.openCameraForImage(this, 0)
        }

        btnGallery.setOnClickListener {
            EasyImage.openGallery(this, 0)
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        EasyImage.handleActivityResult(requestCode, resultCode, data, activity, object : DefaultCallback() {
            override fun onImagesPicked(imageFiles: MutableList<File>, source: EasyImage.ImageSource?, type: Int) {
                CropImage.activity(Uri.fromFile(imageFiles[0])).start(activity!!)
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
            val datapath = "/tesseract/";
            FileUtil.checkFile(
                context!!,
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