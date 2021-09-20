package com.sarathk.sk.kshethra.utilities

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.sarathk.sk.kshethra.R
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


object UtilityFunctions {

    var progressDialog: Dialog? = null

    fun showAlertOnActivity(context: Context, message: String, okButtonMsg: String, cancelButtonMsg: String, showCancelButton: Boolean, setCancelable: Boolean, actionOk: () -> Unit, actionCancel: () -> Unit): AlertDialog? {

        val builder = AlertDialog.Builder(context)
        val inflaterAlert = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewDialog = inflaterAlert.inflate(R.layout.dialog_custom_alert_layout, null)
        builder.setView(viewDialog)
        val dialog = builder.create()

        val buttonCancel = viewDialog.findViewById<Button>(R.id.cancel_button)
        val buttonOk = viewDialog.findViewById<Button>(R.id.ok_button)
        val messageTextView = viewDialog.findViewById<TextView>(R.id.message_tv)

        messageTextView.text = message
        buttonOk.text = okButtonMsg
        buttonCancel.text = cancelButtonMsg

        buttonOk.setOnClickListener {

            dialog.dismiss()
            actionOk()
        }

        if (showCancelButton) {

            buttonCancel.visibility = View.VISIBLE

        } else {

            buttonCancel.visibility = View.GONE
        }
        buttonCancel.setOnClickListener {

            dialog.dismiss()
            actionCancel()
        }

        dialog.setCancelable(setCancelable)
        dialog.show()

        return dialog
    }

    fun showProgressDialog(context: Context): Dialog? {
        if (progressDialog != null && progressDialog?.isShowing!!) {
            return null
        } else {
            progressDialog = Dialog(context)
            progressDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            progressDialog?.setContentView(R.layout.dialog_progress_layout)
            progressDialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            progressDialog?.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

            //val messageTv = progressDialog?.findViewById<TextView>(R.id.message_tv)
            //messageTv?.text = message
            //progressDialog?.setCancelable(cancelable)
            progressDialog?.setCancelable(false)
            progressDialog?.show()

            return progressDialog
        }
    }

    fun hideProgressDialog() {
        if (progressDialog != null && progressDialog?.isShowing!!) {
            progressDialog?.dismiss()
        }
    }

    fun validPassword(password: String): Boolean {
        val hasNumber = password.matches(".*\\d.*".toRegex()) // "a digit with anything before or after"
        val hasSpecial = password.matches(".*[!@#$%^&*].*".toRegex())
        val hasCases = password.matches(".*[a-z].*".toRegex())
        val hasCapCases = password.matches(".*[A-Z].*".toRegex())
        val hasLength = password.length > 7
        return hasCases && hasSpecial && hasNumber && hasCapCases && hasLength
    }

    //fun fromHtml(html: String): Spanned {
    //    val result: Spanned
    //    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
    //        result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    //    } else {
    //        result = Html.fromHtml(html)
    //    }
    //    return result
    //}

    //fun createImageFile(): File {
    //    val root = File(Environment.getExternalStorageDirectory().toString() + File.separator + "CallBlue" + File.separator)
    //    if(!root.exists() || !root.isDirectory) {
    //        root.mkdirs()
    //    }
    //
    //    val image = File.createTempFile("IMG", "CB.jpg", root)
    //
    //    return image
    //}

    fun convertBase64ToBitmap(b64: String): Bitmap? {
        val imageAsBytes: ByteArray = Base64.encode(b64.toByteArray(), Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
    }

    fun scaleDownBitmap(photo: Bitmap?, newHeight: Int, context: Context): Bitmap? {
        var photo_in = photo
        val densityMultiplier = context.resources.displayMetrics.density
        val h = (newHeight * densityMultiplier).toInt()
        val w = (h * photo_in!!.width / photo_in.height.toDouble()).toInt()
        photo_in = Bitmap.createScaledBitmap(photo_in, w, h, true)
        return photo_in
    }

    fun decodeFile(f: File): Bitmap? {
        try {
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(FileInputStream(f), null, o)

            val REQUIRED_SIZE = 500
            var width_tmp = o.outWidth
            var height_tmp = o.outHeight
            var scale = 1
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break
                width_tmp /= 2
                height_tmp /= 2
                scale *= 2
            }

            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            return BitmapFactory.decodeStream(FileInputStream(f), null, o2)

        } catch (ignored: FileNotFoundException) {
        }

        return null
    }
}