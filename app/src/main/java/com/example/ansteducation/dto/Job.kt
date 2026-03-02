package com.example.ansteducation.dto

data class Job(
    val id: Long,
    val name: String,      // company name
    val position: String,  // job position
    val start: String,     // start date (ISO string from backend)
    val finish: String? = null, // finish date or null for current
)

