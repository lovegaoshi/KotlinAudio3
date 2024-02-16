package com.lovegaoshi.kotlinAudio.models

import android.os.Bundle
import androidx.media3.session.CommandButton
import androidx.media3.session.SessionCommand

data class KACommandButton (
    val displayName: String = "",
    val iconRes: Int = 0,
    val sessionCommand: String = "",
    val onLayout: Boolean = false,
    val commandButton: CommandButton = CommandButton.Builder()
        .setDisplayName(displayName)
        .setIconResId(iconRes)
        .setSessionCommand(SessionCommand(sessionCommand, Bundle()))
        .build()
)
