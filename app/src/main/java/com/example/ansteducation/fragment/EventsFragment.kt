package com.example.ansteducation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.R
import com.example.ansteducation.adapter.EventsAdapter
import com.example.ansteducation.adapter.OnEventInteractionListener
import com.example.ansteducation.databinding.FragmentEventsBinding
import com.example.ansteducation.dto.Event
import com.example.ansteducation.viewModel.AuthViewModel
import com.example.ansteducation.viewModel.EventsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventsFragment : Fragment() {

    private lateinit var binding: FragmentEventsBinding
    private val viewModel: EventsViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.menu_events)

        val adapter = EventsAdapter(object : OnEventInteractionListener {
            override fun onLike(event: Event) {
                viewModel.like(event)
            }

            override fun onRemove(event: Event) {
                viewModel.remove(event)
            }

            override fun onEdit(event: Event) {
                findNavController().navigate(
                    R.id.action_eventsFragment_to_newEventFragment,
                    bundleOf(NewEventFragment.ARG_EVENT_ID to event.id),
                )
            }

            override fun onClick(event: Event) {
                findNavController().navigate(
                    R.id.action_eventsFragment_to_eventDetailFragment,
                    bundleOf("eventId" to event.id),
                )
            }
        })
        binding.eventsList.adapter = adapter

        fun refreshEmptyState() {
            val events = viewModel.events.value.orEmpty()
            val err = viewModel.error.value
            val load = viewModel.loading.value == true
            binding.errorText.isVisible = err != null || (events.isEmpty() && !load)
            binding.errorText.text = err ?: if (events.isEmpty()) getString(R.string.no_events) else ""
        }

        viewModel.events.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events)
            refreshEmptyState()
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.progress.isVisible = loading
            refreshEmptyState()
        }

        viewModel.error.observe(viewLifecycleOwner) {
            refreshEmptyState()
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { msg ->
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        }

        binding.addEvent.setOnClickListener {
            if (authViewModel.isAuthorized) {
                findNavController().navigate(R.id.action_eventsFragment_to_newEventFragment)
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.auth_required_title)
                    .setMessage(R.string.auth_required_message)
                    .setPositiveButton(R.string.sign_in) { _, _ ->
                        findNavController().navigate(R.id.authFragment)
                    }
                    .setNeutralButton(R.string.sign_up) { _, _ ->
                        findNavController().navigate(R.id.registerFragment)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }

        if (savedInstanceState == null) {
            viewModel.loadEvents()
        }
    }
}
