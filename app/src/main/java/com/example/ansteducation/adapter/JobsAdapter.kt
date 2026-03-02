package com.example.ansteducation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.ansteducation.databinding.ItemJobBinding
import com.example.ansteducation.dto.Job
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class JobsAdapter(
    private val canEdit: Boolean = false,
    private val onDelete: (Job) -> Unit = {},
) : RecyclerView.Adapter<JobsAdapter.JobViewHolder>() {

    var items: List<Job> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemJobBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JobViewHolder(binding, canEdit, onDelete)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class JobViewHolder(
        private val binding: ItemJobBinding,
        private val canEdit: Boolean,
        private val onDelete: (Job) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Job) {
            binding.companyName.text = job.name
            binding.position.text = job.position

            val formatterIn = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            val formatterOut = DateTimeFormatter.ofPattern("dd MMM yyyy")

            val start = runCatching { OffsetDateTime.parse(job.start, formatterIn) }.getOrNull()
            val finish = job.finish?.let { runCatching { OffsetDateTime.parse(it, formatterIn) }.getOrNull() }

            val startStr = start?.format(formatterOut) ?: ""
            val finishStr = finish?.format(formatterOut) ?: ""

            binding.period.text = if (finishStr.isNotBlank()) {
                "$startStr — $finishStr"
            } else {
                startStr
            }

            binding.deleteButton.isVisible = canEdit
            binding.deleteButton.setOnClickListener {
                onDelete(job)
            }
        }
    }
}

