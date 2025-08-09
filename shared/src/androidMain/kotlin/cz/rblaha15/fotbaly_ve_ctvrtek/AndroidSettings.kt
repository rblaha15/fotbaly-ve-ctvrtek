package cz.rblaha15.fotbaly_ve_ctvrtek

import android.content.Context
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings

fun settings(context: Context): ObservableSettings =
    SharedPreferencesSettings.Factory(context).create("fotbaly_ve_ctvrtek")