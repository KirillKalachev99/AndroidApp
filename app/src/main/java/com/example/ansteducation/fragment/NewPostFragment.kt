package com.example.ansteducation.fragment

import com.example.ansteducation.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.activity.AppActivity
import com.example.ansteducation.activity.AppActivity.Companion.textArg
import com.example.ansteducation.databinding.FragmentNewPostBinding
import com.example.ansteducation.util.AndroidUtils
import com.example.ansteducation.viewModel.PostViewModel

class NewPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)


        arguments?.textArg?.let(binding.content::setText)
        binding.apply {
            newPostLo.isVisible = true
            addPhoto.setOnClickListener {

            }
        }
        viewModel.edited.value?.content?.let {
            binding.content.setText(it)
        }

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
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
        (activity as? AppActivity)?.showActionBar(true)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.new_post)
    }
}
