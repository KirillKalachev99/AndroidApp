package com.example.ansteducation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.ansteducation.databinding.FragmentFullscreenImageBinding

class FullscreenImageFragment : Fragment() {

    private var _binding: FragmentFullscreenImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFullscreenImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUrl = arguments?.getString("imageUrl")

        imageUrl?.let { url ->
            loadFullscreenImage(url)
        } ?: run {
            findNavController().navigateUp()
        }

        binding.fullscreenImage.setOnClickListener {
            findNavController().navigateUp()
        }

        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.hide()
    }

    private fun loadFullscreenImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.fullscreenImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.show()
        _binding = null
    }
}