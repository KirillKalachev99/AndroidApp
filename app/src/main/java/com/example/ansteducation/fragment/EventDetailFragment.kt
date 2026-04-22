package com.example.ansteducation.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.ansteducation.R
import com.example.ansteducation.databinding.FragmentEventDetailBinding
import com.example.ansteducation.dto.Event
import com.example.ansteducation.dto.EventType
import com.example.ansteducation.dto.User
import com.example.ansteducation.util.formatApiDateTime
import com.example.ansteducation.viewModel.EventDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventDetailFragment : Fragment() {

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventDetailViewModel by viewModels()

    private var lastEvent: Event? = null
    private var lastUsers: List<User> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventId = requireArguments().getLong(ARG_EVENT_ID)

        fun refreshLists() {
            val ev = lastEvent ?: return
            binding.participants.text = viewModel.namesForIds(ev.participantsIds, lastUsers)
            binding.speakers.text = viewModel.namesForIds(ev.speakerIds, lastUsers)
        }

        fun bindEvent(event: Event) {
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                "${event.author} — ${getString(R.string.menu_events)}"

            binding.content.text = event.content
            binding.datetime.text = formatApiDateTime(event.datetime)
            binding.type.text = if (event.type == EventType.OFFLINE) {
                getString(R.string.event_offline)
            } else {
                getString(R.string.event_online)
            }

            val job = event.authorJob?.takeIf { it.isNotBlank() }
                ?: getString(R.string.author_job_searching)
            binding.authorJob.text = job

            val c = event.coords
            if (c != null) {
                binding.mapLink.isVisible = true
                binding.mapLink.setOnClickListener {
                    val uri = Uri.parse("geo:${c.lat},${c.lon}?q=${c.lat},${c.lon}")
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            } else {
                binding.mapLink.isVisible = false
            }

            refreshLists()
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            binding.progress.isVisible = event == null && viewModel.error.value == null
            lastEvent = event
            if (event != null) {
                bindEvent(event)
            }
        }

        viewModel.users.observe(viewLifecycleOwner) { users ->
            lastUsers = users
            refreshLists()
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            binding.errorText.isVisible = err != null
            binding.errorText.text = err.orEmpty()
            binding.progress.isVisible = err == null && viewModel.event.value == null
        }

        if (savedInstanceState == null) {
            viewModel.load(eventId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_EVENT_ID = "eventId"
    }
}
