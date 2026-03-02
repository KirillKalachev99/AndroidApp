package com.example.ansteducation.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.R
import com.example.ansteducation.databinding.FragmentNewEventBinding
import com.example.ansteducation.dto.Event
import com.example.ansteducation.dto.EventType
import com.example.ansteducation.util.AndroidUtils
import com.example.ansteducation.viewModel.EventsViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private lateinit var binding: FragmentNewEventBinding
    private val viewModel: EventsViewModel by activityViewModels()
    private val maxSizePx = 2048
    private var selectedDateTime: LocalDateTime? = null
    private var attachmentFile: java.io.File? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewEventBinding.inflate(inflater, container, false)

        val imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val uri = result.data?.data
            if (result.resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(requireContext(), R.string.failed_photo, Toast.LENGTH_SHORT).show()
            } else if (uri != null) {
                attachmentFile = uri.toFile()
                binding.previewContainer.isVisible = true
                binding.removePhoto.isVisible = true
                binding.preview.setImageURI(uri)
            }
        }

        binding.removePhoto.setOnClickListener {
            attachmentFile = null
            binding.previewContainer.isVisible = false
        }

        binding.openCamera.setOnClickListener {
            ImagePicker.with(this).cameraOnly().crop().maxResultSize(maxSizePx, maxSizePx)
                .createIntent {
                    imagePickerLauncher.launch(it)
                }
        }

        binding.addPhoto.setOnClickListener {
            ImagePicker.with(this).galleryOnly().crop().maxResultSize(maxSizePx, maxSizePx)
                .createIntent {
                    imagePickerLauncher.launch(it)
                }
        }

        binding.dateButton.setOnClickListener {
            showDateTimePicker()
        }

        binding.locationButton.setOnClickListener {
            // TODO: переход на экран выбора локации
        }

        binding.speakersButton.setOnClickListener {
            // TODO: выбор списка спикеров
        }

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.new_post_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.sava_button -> {
                            val content = binding.content.text.toString()
                            val datetime = selectedDateTime
                            if (content.isBlank() || datetime == null) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_empty_content),
                                    Toast.LENGTH_LONG
                                ).show()
                                return true
                            }

                            val type = if (binding.typeOffline.isChecked) {
                                EventType.OFFLINE
                            } else {
                                EventType.ONLINE
                            }

                            val event = Event(
                                id = 0,
                                authorId = 0,
                                author = "",
                                content = content,
                                published = "",
                                datetime = datetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                type = type,
                            )

                            // Пока без вложений и спикеров
                            // Сохраняем событие через ViewModel
                            // (добавим отдельный метод saveEvent в EventsViewModel)
                            // Здесь просто добавим в список локально
                            // TODO: заменить на полноценный вызов API сохранения

                            AndroidUtils.hideKeyboard(requireView())
                            findNavController().navigateUp()
                            true
                        }

                        else -> false
                    }
            },
            viewLifecycleOwner,
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.menu_events)
    }

    private fun showDateTimePicker() {
        val now = LocalDateTime.now()
        val dateDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val timeDialog = TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        selectedDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute)
                        binding.dateButton.text = selectedDateTime.toString()
                    },
                    now.hour,
                    now.minute,
                    true
                )
                timeDialog.show()
            },
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        )
        dateDialog.show()
    }
}

