package com.example.notelite3.ui.util


import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTime(millis: Long): String =
    SimpleDateFormat("MM-dd hh:mm a", Locale.US).format(Date(millis))