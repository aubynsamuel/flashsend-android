package com.aubynsamuel.flashsend.core.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class DropMenu(
    val text: String = "",
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
)