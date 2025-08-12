package com.example.ansteducation.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ansteducation.R
import com.example.ansteducation.databinding.ActivityNewPostBinding

private const val ADD_CODE = 101
private const val EDIT_CODE = 102
private const val CODE_NAME = "code"

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editingText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!editingText.isNullOrBlank()) {
            binding.content.setText(editingText)
            binding.content.setSelection(editingText.length)
        }
        val editMode = intent.getIntExtra("code", EDIT_CODE)
        if (editMode == EDIT_CODE){
            binding.add.setImageResource(R.drawable.ic_edit_48)
        }
        binding.add.setOnClickListener {
            val text = binding.content.text.toString()
            if (text.isBlank()) setResult(RESULT_CANCELED)
            else {
                setResult(RESULT_OK, Intent().apply { putExtra(Intent.EXTRA_TEXT, text) })
            }
            finish()
        }
        binding.content.requestFocus()
    }
}

object NewPostContract : ActivityResultContract<Unit, String?>(){
    override fun createIntent(context: Context, input: Unit) = Intent(context, NewPostActivity::class.java).apply {
        putExtra(CODE_NAME, ADD_CODE)
    }

    override fun parseResult(resultCode: Int, intent: Intent?) = intent?.getStringExtra(Intent.EXTRA_TEXT)
}

object EditPostContract : ActivityResultContract<String, String?>() {
    override fun createIntent(context: Context, input: String) =
        Intent(context, NewPostActivity::class.java).apply {
            putExtra(Intent.EXTRA_TEXT, input)
            putExtra(CODE_NAME, EDIT_CODE)
        }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        if (resultCode == Activity.RESULT_OK) {
            intent?.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }
}