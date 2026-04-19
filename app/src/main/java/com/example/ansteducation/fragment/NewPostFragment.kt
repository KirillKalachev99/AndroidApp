package com.example.ansteducation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.R
import com.example.ansteducation.activity.AppActivity.Companion.textArg
import com.example.ansteducation.api.UserApi
import com.example.ansteducation.databinding.FragmentNewPostBinding
import com.example.ansteducation.util.AndroidUtils
import com.example.ansteducation.viewModel.PostViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    @Inject
    lateinit var userApi: UserApi

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    private val maxSizePx = 2048
    private val maxAttachmentBytes = 15L * 1024 * 1024

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
            if (file != null) {
                viewModel.changePhoto(uri, file)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)

        viewModel.photo.observe(viewLifecycleOwner) { photo ->
            if (photo != null) {
                binding.previewContainer.isVisible = true
                binding.removePhoto.isVisible = true
                binding.preview.setImageURI(photo.uri)
            } else {
                binding.previewContainer.isVisible = false
            }
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { msg ->
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        }

        viewModel.mentionIds.observe(viewLifecycleOwner) { ids ->
            binding.mentionsButton.text = if (ids.isEmpty()) {
                getString(R.string.post_mentions)
            } else {
                getString(R.string.speakers_selected, ids.size)
            }
        }

        binding.removePhoto.setOnClickListener {
            viewModel.removePhoto()
        }

        arguments?.textArg?.let(binding.content::setText)
        binding.newPostLo.isVisible = true

        binding.openCamera.setOnClickListener {
            ImagePicker.with(this).cameraOnly().crop().maxResultSize(maxSizePx, maxSizePx)
                .createIntent { imagePickerLauncher.launch(it) }
        }

        binding.addPhoto.setOnClickListener {
            ImagePicker.with(this).galleryOnly().crop().maxResultSize(maxSizePx, maxSizePx)
                .createIntent { imagePickerLauncher.launch(it) }
        }

        binding.mentionsButton.setOnClickListener {
            showMentionsDialog()
        }

        viewModel.edited.value?.content?.let {
            binding.content.setText(it)
        }

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.new_post_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.sava_button -> {
                            val content = binding.content.text.toString().trim()
                            if (content.isBlank()) {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.error_empty_content,
                                    Toast.LENGTH_SHORT,
                                ).show()
                                true
                            } else {
                                viewModel.save(content)
                                AndroidUtils.hideKeyboard(requireView())
                                findNavController().navigateUp()
                                true
                            }
                        }

                        else -> false
                    }
            },
            viewLifecycleOwner,
        )

        binding.content.requestFocus()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.new_post)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showMentionsDialog() {
        lifecycleScope.launch {
            val users = runCatching { userApi.getAll() }.getOrElse {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                return@launch
            }
            val selected = viewModel.mentionIds.value.orEmpty().toMutableList()
            val labels = users.map { u -> "${u.name} (@${u.login})" }.toTypedArray()
            val checked = BooleanArray(users.size) { idx -> selected.contains(users[idx].id) }

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.post_mentions)
                .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                    val id = users[which].id
                    if (isChecked) {
                        if (!selected.contains(id)) selected.add(id)
                    } else {
                        selected.remove(id)
                    }
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.setMentionIds(selected)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }
}
