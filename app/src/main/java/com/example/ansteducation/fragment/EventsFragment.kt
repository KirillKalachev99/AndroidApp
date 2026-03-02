package com.example.ansteducation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.R
import com.example.ansteducation.adapter.EventsAdapter
import com.example.ansteducation.adapter.OnEventInteractionListener
import com.example.ansteducation.databinding.FragmentEventsBinding
import com.example.ansteducation.dto.Event
import com.example.ansteducation.viewModel.AuthViewModel
import com.example.ansteducation.viewModel.EventsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventsFragment : Fragment() {

    private lateinit var binding: FragmentEventsBinding
    private val viewModel: EventsViewModel by viewModels()
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
            override fun like(event: Event) {
                viewModel.like(event)
            }

            override fun remove(event: Event) {
                viewModel.remove(event)
            }

            override fun onClick(event: Event) {
                TODO()
            }
        })
        binding.eventsList.adapter = adapter

        viewModel.events.observe(viewLifecycleOwner) { events ->
            adapter.items = events
            binding.errorText.isVisible = events.isEmpty() && !viewModel.loading.value!!
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.progress.isVisible = loading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.errorText.isVisible = error != null
            binding.errorText.text = error ?: ""
        }

        binding.addEvent.setOnClickListener {
            if (authViewModel.isAuthorized) {
                findNavController().navigate(R.id.newEventFragment)
            } else {
                findNavController().navigate(R.id.authFragment)
            }
        }

        if (savedInstanceState == null) {
            viewModel.loadEvents()
        }
    }
}

