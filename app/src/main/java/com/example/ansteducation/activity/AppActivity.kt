package com.example.ansteducation.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.example.ansteducation.R
import com.example.ansteducation.auth.AppAuth
import com.example.ansteducation.databinding.ActivityAppBinding
import com.example.ansteducation.dto.Token
import com.example.ansteducation.util.StringArg
import com.example.ansteducation.viewModel.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class AppActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationsPermission()
        enableEdgeToEdge()
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar

        intent?.let {
            if (it.action != Intent.ACTION_SEND) return@let

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text.isNullOrBlank()) {
                Snackbar.make(
                    binding.root,
                    R.string.error_empty_content,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(android.R.string.ok) {
                        finish()
                    }.show()
                return@let
            }
            findNavController(R.id.nav_host_fragment).navigate(
                R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply {
                    textArg = text
                }
            )
        }

        addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.menu_auth, menu)
                    authViewModel.data.observe(this@AppActivity) {
                        val authorized = authViewModel.isAuthorized
                        menu.setGroupVisible(R.id.authorized, authorized)
                        menu.setGroupVisible(R.id.unAuthorized, !authorized)
                    }
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.signIn, R.id.signUp -> {
                            findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_authFragment)
                            //AppAuth.getInstance().setAuth(Token(5, "x-token"))
                            true
                        }

                        R.id.logout -> {
                            //TODO remove hardcode
                            AppAuth.getInstance().clear()
                            true
                        }

                        else -> false
                    }
            }
        )
    }


    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = android.Manifest.permission.POST_NOTIFICATIONS
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        requestPermissions(arrayOf(permission), 1)
    }

    fun showActionBar(show: Boolean) {
        supportActionBar?.let {
            if (show) {
                it.show()
            } else {
                it.hide()
            }
        }
    }
}