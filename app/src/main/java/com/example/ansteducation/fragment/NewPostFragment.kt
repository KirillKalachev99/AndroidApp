package com.example.ansteducation.fragment

import com.example.ansteducation.R
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
import androidx.fragment.app.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.activity.AppActivity
import com.example.ansteducation.activity.AppActivity.Companion.textArg
import com.example.ansteducation.databinding.FragmentNewPostBinding
import com.example.ansteducation.util.AndroidUtils
import com.example.ansteducation.viewModel.PostViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    private val maxSizePx = 2048

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)
        val imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val uri = result.data?.data
            if (result.resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(requireContext(), R.string.failed_photo, Toast.LENGTH_SHORT).show()
            } else if (uri != null) {
                viewModel.changePhoto(uri, uri.toFile())
            }
        }

        viewModel.photo.observe(viewLifecycleOwner) { photo ->
            if (photo != null) {
                binding.previewContainer.isVisible = true
                binding.removePhoto.isVisible = true
                binding.preview.setImageURI(photo.uri)
            } else {
                binding.previewContainer.isVisible = false
            }
        }

        binding.removePhoto.setOnClickListener {
            viewModel.removePhoto()
        }

        arguments?.textArg?.let(binding.content::setText)
        binding.newPostLo.isVisible = true

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

        viewModel.edited.value?.content?.let {
            binding.content.setText(it)
        }

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu, menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.new_post_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.sava_button -> {
                            val content = binding.content.text.toString()
                            if (content.isNotBlank()) {
                                viewModel.save(content)
                                AndroidUtils.hideKeyboard(requireView())
                                findNavController().navigateUp()
                                true
                            } else {
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
}
