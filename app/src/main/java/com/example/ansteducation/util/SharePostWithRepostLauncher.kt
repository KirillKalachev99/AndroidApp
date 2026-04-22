package com.example.ansteducation.util

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.ansteducation.R
import com.example.ansteducation.dto.Post

/**
 * Opens the system share sheet and calls [onShareCompleted] only if the user did not cancel
 * the chooser (result is not [Activity.RESULT_CANCELED]).
 *
 * Note: some OEMs return CANCEL even after a target app was chosen; in that edge case the
 * counter may not update until the feed refreshes from the server.
 */
class SharePostWithRepostLauncher(
    private val fragment: Fragment,
    private val onShareCompleted: (Long) -> Unit,
) {
    private var pendingPostId: Long? = null

    private val launcher: ActivityResultLauncher<Intent> =
        fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val id = pendingPostId
            pendingPostId = null
            if (id == null) return@registerForActivityResult
            if (result.resultCode != Activity.RESULT_CANCELED) {
                onShareCompleted(id)
            }
        }

    fun launch(post: Post) {
        pendingPostId = post.id
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, post.content)
        }
        launcher.launch(
            Intent.createChooser(
                send,
                fragment.getString(R.string.chooser_share_post),
            ),
        )
    }
}
