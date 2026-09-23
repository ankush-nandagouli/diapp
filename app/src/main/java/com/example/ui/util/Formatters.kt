package com.example.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val inrFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun formatCurrency(amount: Double): String {
        return try {
            inrFormat.format(amount)
        } catch (e: Exception) {
            "₹%,.0f".format(amount)
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
