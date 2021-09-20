package com.sarathk.sk.kshethra.extensions

import android.app.Activity
import android.content.Context
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import com.sarathk.sk.kshethra.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Hides the soft keyboard
 */
fun Activity.hideKeyboard(): Boolean {
    val view = currentFocus
    view?.let {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
    return false
}

fun AppCompatActivity.hideKeyboard(): Boolean {
    val view = currentFocus
    view?.let {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
    return false
}

fun Activity.customTitle(title: String) {
    actionBar?.setDisplayShowTitleEnabled(false)
    actionBar?.setDisplayShowCustomEnabled(true)

    val titleTextView = TextView(this)
    val titleLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    titleTextView.layoutParams = titleLayoutParams
    titleTextView.text = title
    titleTextView.textSize = 18f
    titleTextView.isAllCaps = true
    titleTextView.typeface = ResourcesCompat.getFont(applicationContext, R.font.roboto_bold)
    titleTextView.setTextColor(Color.WHITE)
    titleTextView.gravity = Gravity.CENTER_VERTICAL
    actionBar?.customView = titleTextView
}

fun AppCompatActivity.customTitle(title: String) {
    supportActionBar?.setDisplayShowTitleEnabled(false)
    supportActionBar?.setDisplayShowCustomEnabled(true)

    val titleTextView = TextView(this)
    val titleLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    titleTextView.layoutParams = titleLayoutParams
    titleTextView.text = title
    titleTextView.textSize = 18f
    titleTextView.isAllCaps = true
    titleTextView.typeface = ResourcesCompat.getFont(applicationContext, R.font.roboto_bold)
    titleTextView.setTextColor(Color.WHITE)
    titleTextView.gravity = Gravity.CENTER_VERTICAL
    supportActionBar?.customView = titleTextView
}

fun <R> CoroutineScope.executeAsyncTask(
    onPreExecute: () -> Unit,
    doInBackground: () -> R,
    onPostExecute: (R) -> Unit
) = launch {
    onPreExecute() // runs in Main Thread
    val result = withContext(Dispatchers.IO) {
        doInBackground() // runs in background thread without blocking the Main Thread
    }
    onPostExecute(result) // runs in Main Thread
}


