package com.health.nutritionai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A meal photo captured while offline, queued for upload once connectivity returns.
 * [imagePath] points at a copy in app-internal storage so it survives independent of
 * wherever the original capture temp file lives.
 */
@Entity(tableName = "pending_meals")
data class PendingMealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val imagePath: String,
    val mealType: String?,
    val createdAt: Long = System.currentTimeMillis()
)
