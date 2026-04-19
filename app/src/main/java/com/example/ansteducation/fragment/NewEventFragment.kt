package com.example.ansteducation.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.R
import com.example.ansteducation.databinding.FragmentNewEventBinding
import com.example.ansteducation.dto.Coordinates
import com.example.ansteducation.dto.Event
import com.example.ansteducation.dto.EventType
import com.example.ansteducation.util.AndroidUtils
import com.example.ansteducation.viewModel.EventsViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.example.ansteducation.api.EventApi
import com.example.ansteducation.api.UserApi

@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private lateinit var binding: FragmentNewEventBinding
    private val eventsViewModel: EventsViewModel by activityViewModels()

    @Inject
    lateinit var eventApi: EventApi

    @Inject
    lateinit var userApi: UserApi

    private val maxSizePx = 2048
    private val maxAttachmentBytes = 15L * 1024 * 1024

    private var selectedDateTime: LocalDateTime? = null
    private var attachmentFile: File? = null
    private val speakerIds = mutableListOf<Long>()
    private var editingEventId: Long = 0L
    private var selectedCoords: Coordinates? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (result.resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), R.string.failed_photo, Toast.LENGTH_SHORT).show()
        } else if (uri != null) {
            val file = runCatching { uri.toFile() }.getOrNull()
            if (file != null && file.length() > maxAttachmentBytes) {
                Toast.makeText(requireContext(), R.string.attachment_too_large, Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }
            attachmentFile = file
            binding.previewContainer.isVisible = true
            binding.removePhoto.isVisible = true
            binding.preview.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editingEventId = arguments?.getLong(ARG_EVENT_ID) ?: 0L

        (activity as? AppCompatActivity)?.supportActionBar?.title =
            if (editingEventId != 0L) getString(R.string.edit_event) else getString(R.string.new_event)

        binding.removePhoto.setOnClickListener {
            attachmentFile = null
            binding.previewContainer.isVisible = false
        }

        binding.openCamera.setOnClickListener {
            ImagePicker.with(this).cameraOnly().crop().maxResultSize(maxSizePx, maxSizePx)
                .createIntent { imagePickerLauncher.launch(it) }
        }

        binding.addPhoto.setOnClickListener {
            ImagePicker.with(this).galleryOnly().crop().maxResultSize(maxSizePx, maxSizePx)
                .createIntent { imagePickerLauncher.launch(it) }
        }

        binding.dateButton.setOnClickListener {
            showDateTimePicker()
        }

        binding.locationButton.setOnClickListener {
            showLocationDialog()
        }

        binding.speakersButton.setOnClickListener {
            showSpeakersDialog()
        }

        eventsViewModel.saveFinished.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        eventsViewModel.snackbarMessage.observe(viewLifecycleOwner) { msg ->
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        }

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.new_post_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.sava_button -> {
                            saveEvent()
                            true
                        }

                        else -> false
                    }
            },
            viewLifecycleOwner,
        )

        if (editingEventId != 0L && savedInstanceState == null) {
            lifecycleScope.launch {
                runCatching { eventApi.getById(editingEventId) }
                    .onSuccess { ev ->
                        binding.content.setText(ev.content)
                        binding.linkInput.setText(ev.link.orEmpty())
                        if (ev.type == EventType.OFFLINE) {
                            binding.typeOffline.isChecked = true
                        } else {
                            binding.typeOnline.isChecked = true
                        }
                        speakerIds.clear()
                        speakerIds.addAll(ev.speakerIds)
                        binding.speakersButton.text =
                            getString(R.string.speakers_selected, speakerIds.size)
                        selectedDateTime = runCatching {
                            java.time.OffsetDateTime.parse(ev.datetime).toLocalDateTime()
                        }.getOrElse {
                            LocalDateTime.parse(ev.datetime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        }
                        binding.dateButton.text = selectedDateTime.toString()
                        selectedCoords = ev.coords
                        updateLocationButtonLabel()
                    }
                    .onFailure {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun saveEvent() {
        val content = binding.content.text.toString().trim()
        val datetime = selectedDateTime
        if (content.isBlank() || datetime == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.event_validation_error),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val type = if (binding.typeOffline.isChecked) EventType.OFFLINE else EventType.ONLINE
        val link = binding.linkInput.text.toString().trim().takeIf { it.isNotBlank() }

        val event = Event(
            id = editingEventId,
            authorId = 0,
            author = "",
            content = content,
            published = "",
            datetime = datetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            type = type,
            speakerIds = speakerIds.toList(),
            link = link,
            coords = selectedCoords,
        )

        AndroidUtils.hideKeyboard(requireView())
        eventsViewModel.saveEvent(event, attachmentFile)
    }

    private fun updateLocationButtonLabel() {
        binding.locationButton.text = selectedCoords?.let { c ->
            getString(R.string.location_coords_set, c.lat, c.lon)
        } ?: getString(R.string.select_location)
    }

    private fun showLocationDialog() {
        val ctx = requireContext()
        val pad = (16 * resources.displayMetrics.density).toInt()
        val type = InputType.TYPE_CLASS_NUMBER or
            InputType.TYPE_NUMBER_FLAG_DECIMAL or
            InputType.TYPE_NUMBER_FLAG_SIGNED
        val latInput = EditText(ctx).apply {
            hint = getString(R.string.location_lat_hint)
            inputType = type
            setSingleLine(true)
        }
        val lonInput = EditText(ctx).apply {
            hint = getString(R.string.location_lon_hint)
            inputType = type
            setSingleLine(true)
        }
        selectedCoords?.let { c ->
            latInput.setText(c.lat.toString())
            lonInput.setText(c.lon.toString())
        }
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            addView(
                latInput,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ),
            )
            addView(
                lonInput,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
        AlertDialog.Builder(ctx)
            .setTitle(R.string.select_location)
            .setView(layout)
            .setPositiveButton(R.string.save) { _, _ ->
                val la = latInput.text.toString().replace(',', '.').toDoubleOrNull()
                val lo = lonInput.text.toString().replace(',', '.').toDoubleOrNull()
                if (la != null && lo != null) {
                    selectedCoords = Coordinates(la, lo)
                    updateLocationButtonLabel()
                } else {
                    Toast.makeText(ctx, R.string.invalid_coordinates, Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton(R.string.location_clear) { _, _ ->
                selectedCoords = null
                updateLocationButtonLabel()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showSpeakersDialog() {
        lifecycleScope.launch {
            val users = runCatching { userApi.getAll() }.getOrElse {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                return@launch
            }
            val labels = users.map { u -> "${u.name} (@${u.login})" }.toTypedArray()
            val checked = BooleanArray(users.size) { idx -> speakerIds.contains(users[idx].id) }

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_speakers)
                .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                    val id = users[which].id
                    if (isChecked) {
                        if (!speakerIds.contains(id)) speakerIds.add(id)
                    } else {
                        speakerIds.remove(id)
                    }
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    binding.speakersButton.text =
                        getString(R.string.speakers_selected, speakerIds.size)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun showDateTimePicker() {
        val now = LocalDateTime.now()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        selectedDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute)
                        binding.dateButton.text = selectedDateTime.toString()
                    },
                    now.hour,
                    now.minute,
                    true
                ).show()
            },
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        ).show()
    }

    companion object {
        const val ARG_EVENT_ID = "eventId"
    }
}
