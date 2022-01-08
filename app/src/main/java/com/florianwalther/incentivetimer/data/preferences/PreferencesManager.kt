package com.florianwalther.incentivetimer.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import logcat.asLog
import logcat.logcat
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class TimerPreferences(
    val pomodoroLengthInMinutes: Int,
    val shortBreakLengthInMinutes: Int,
    val longBreakLengthInMinutes: Int,
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

    val timerPreferences: Flow<TimerPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                logcat { exception.asLog() }
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val pomodoroLengthInMinutes = preferences[PreferencesKeys.POMODORO_LENGTH_IN_MINUTES]
                ?: POMODORO_LENGTH_IN_MINUTES_DEFAULT
            val shortBreakLengthInMinutes =
                preferences[PreferencesKeys.SHORT_BREAK_LENGTH_IN_MINUTES]
                    ?: SHORT_BREAK_LENGTH_IN_MINUTES_DEFAULT
            val longBreakLengthInMinutes =
                preferences[PreferencesKeys.LONG_BREAK_LENGTH_IN_MINUTES]
                    ?: LONG_BREAK_LENGTH_IN_MINUTES_DEFAULT
            TimerPreferences(
                pomodoroLengthInMinutes = pomodoroLengthInMinutes,
                shortBreakLengthInMinutes = shortBreakLengthInMinutes,
                longBreakLengthInMinutes = longBreakLengthInMinutes,
            )
        }

    suspend fun updatePomodoroLength(lengthInMinutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.POMODORO_LENGTH_IN_MINUTES] = lengthInMinutes
        }
    }

    suspend fun updateShortBreakLength(lengthInMinutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHORT_BREAK_LENGTH_IN_MINUTES] = lengthInMinutes
        }
    }

    suspend fun updateLongBreakLength(lengthInMinutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LONG_BREAK_LENGTH_IN_MINUTES] = lengthInMinutes
        }
    }

    private object PreferencesKeys {
        val POMODORO_LENGTH_IN_MINUTES = intPreferencesKey("POMODORO_LENGTH_IN_MINUTES")
        val SHORT_BREAK_LENGTH_IN_MINUTES = intPreferencesKey("SHORT_BREAK_LENGTH_IN_MINUTES")
        val LONG_BREAK_LENGTH_IN_MINUTES = intPreferencesKey("LONG_BREAK_LENGTH_IN_MINUTES")
    }
}

const val POMODORO_LENGTH_IN_MINUTES_DEFAULT = 25
const val SHORT_BREAK_LENGTH_IN_MINUTES_DEFAULT = 5
const val LONG_BREAK_LENGTH_IN_MINUTES_DEFAULT = 15