package dev.euryperez.later.data

import androidx.annotation.DrawableRes
import dev.euryperez.later.R

enum class Category(val label: String, @DrawableRes val cover: Int) {
    WORK("Work", R.drawable.cover_work),
    HOME("Home", R.drawable.cover_home),
    HEALTH("Health", R.drawable.cover_health),
    ADMIN("Admin", R.drawable.cover_admin),
    FUN("Fun", R.drawable.cover_fun),
}
