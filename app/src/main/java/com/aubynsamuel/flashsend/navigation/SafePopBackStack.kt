package com.aubynsamuel.flashsend.navigation

import androidx.navigation.NavController

fun NavController.safePopBackStack() {
    if (this.previousBackStackEntry != null) {
        this.popBackStack()
    }
}
