package com.resekollen

import android.app.Application
import com.resekollen.data.FavoritesStore
import com.resekollen.data.TravelRepository

/** Håller en delad instans av repository och favoriter för hela appen. */
class ReseKollenApp : Application() {
    val repository: TravelRepository by lazy { TravelRepository(this) }
    val favorites: FavoritesStore by lazy { FavoritesStore(this) }
}
