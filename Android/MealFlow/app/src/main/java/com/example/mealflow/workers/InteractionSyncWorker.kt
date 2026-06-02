package com.example.mealflow.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mealflow.database.CommunityDatabase
import com.example.mealflow.network.ApiInteract
import com.example.mealflow.network.InteractionRequest

class InteractionSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val interactionDao = CommunityDatabase.getDatabase(applicationContext).pendingInteractionDao()
    private val apiInteract = ApiInteract()

    override suspend fun doWork(): Result {
        val pendingInteractions = interactionDao.getAll()

        if (pendingInteractions.isEmpty()) {
            return Result.success()
        }

        var allSucceeded = true
        for (interaction in pendingInteractions) {
            val request = when (interaction.interactionType) {
                "cooked" -> InteractionRequest(
                    userId = interaction.userId,
                    mealId = interaction.mealId,
                    type = interaction.interactionType,
                    cooked = true,
                    liked = false,
                    disliked = false,
                    ignored = false,
                    viewed = false,
                    saved = false,
                    favorited = false,
                    rating = null,
                    reviewText = null
                )
                "liked" -> InteractionRequest(
                    userId = interaction.userId,
                    mealId = interaction.mealId,
                    type = interaction.interactionType,
                    liked = true,
                    cooked = false,
                    disliked = false,
                    ignored = false,
                    viewed = false,
                    saved = false,
                    favorited = false,
                    rating = null,
                    reviewText = null
                )
                "review" -> InteractionRequest(
                    userId = interaction.userId,
                    mealId = interaction.mealId,
                    type = interaction.interactionType,
                    reviewText = interaction.reviewText,
                    cooked = false,
                    liked = false,
                    disliked = false,
                    ignored = false,
                    viewed = true,
                    saved = false,
                    favorited = false,
                    rating = null
                )
                else -> InteractionRequest(
                    userId = interaction.userId,
                    mealId = interaction.mealId,
                    type = interaction.interactionType,
                    cooked = false,
                    liked = false,
                    disliked = false,
                    ignored = false,
                    viewed = true,
                    saved = false,
                    favorited = false,
                    rating = null,
                    reviewText = null
                )
            }

            val result = apiInteract.sendInteraction(request)
            if (result.isSuccess) {
                interactionDao.deleteById(interaction.id)
            } else {
                allSucceeded = false
                Log.e("InteractionSyncWorker", "Failed to sync interaction for meal ${interaction.mealId}")
            }
        }

        return if (allSucceeded) Result.success() else Result.retry()
    }
} 