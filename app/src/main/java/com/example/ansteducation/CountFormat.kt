package com.example.ansteducation

import android.icu.text.DecimalFormat

const val THOUSAND = 1_000
const val TEN_THOUSAND = 10_000
const val MILLION = 1_000_000

object CountFormat {
    fun format (count: Int): String{
        val formatedCount: String
        val format = DecimalFormat("#.#")
        if (count in THOUSAND..<TEN_THOUSAND){
            val divided = count.toFloat().div(THOUSAND)
            formatedCount = format.format(divided).toString() + "K"
        }
        else if (count in TEN_THOUSAND..MILLION) {
            val counted = count.floorDiv(THOUSAND)
            formatedCount = counted.toString() + "K"
        }
        else if (count > MILLION){
            val devidedMil = count.toFloat().div(MILLION)
            formatedCount = format.format(devidedMil).toString() + "M"
        }
        else {
            formatedCount = count.toString()
        }
        return formatedCount
    }
}