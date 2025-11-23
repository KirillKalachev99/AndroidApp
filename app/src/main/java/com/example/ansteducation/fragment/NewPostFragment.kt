package com.example.ansteducation.fragment

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
    ): View? {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)

        arguments?.textArg?.let(binding.content::setText)

        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.edited.value?.content?.let {
            binding.apply {
                content.setText(it)
                add.setImageResource(R.drawable.ic_menu_edit)
            }
        }

        binding.add.setOnClickListener {
            val content = binding.content.text.toString()
            if (content.isNotBlank()) {
                viewModel.save(content)
                AndroidUtils.hideKeyboard(requireView())
                findNavController().navigateUp()
            }
        }
        binding.content.requestFocus()

        return binding.root
    }
}
