package com.dgero.homly.auth.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dgero.homly.auth.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class DataStoreSessionRepository(private val context: Context) : SessionRepository {

    private val currentUserIdKey = longPreferencesKey("current_user_id")

    override val currentUserId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[currentUserIdKey]
    }

    override suspend fun setSession(userId: Long) {
        context.dataStore.edit { prefs -> prefs[currentUserIdKey] = userId }
    }

    override suspend fun clear() {
        context.dataStore.edit { prefs -> prefs.remove(currentUserIdKey) }
    }
}
