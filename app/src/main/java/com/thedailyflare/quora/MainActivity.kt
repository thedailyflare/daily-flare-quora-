package com.thedailyflare.quora

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray

class MainActivity : Activity() {
 private val feedBase="https://thedailyflare.com/wp-json/wp/v2/posts?per_page=30&_fields=link,title,excerpt,date"
 private val quoraSpace="https://thedailyflare.quora.com/"
 private val searchConsoleBase="https://search.google.com/search-console/inspect?resource_id=https://thedailyflare.com/&id="
 private val posted by lazy { getSharedPreferences("posted", MODE_PRIVATE) }
 private val navy=Color.rgb(23,42,58); private val ink=Color.rgb(32,38,43)
 private val muted=Color.rgb(105,113,120); private val cream=Color.rgb(247,246,243)
 private val gold=Color.rgb(201,168,106); private val green=Color.rgb(71,117,91)
