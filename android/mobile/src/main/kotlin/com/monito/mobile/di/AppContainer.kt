package com.monito.mobile.di

import android.content.Context
import com.monito.mobile.data.remote.RetrofitProvider
import com.monito.mobile.data.repository.DevicePreferences
import com.monito.mobile.data.repository.PetRepositoryImpl
import com.monito.mobile.data.repository.WeatherRepositoryImpl
import com.monito.mobile.domain.repository.PetRepository
import com.monito.mobile.domain.repository.WeatherRepository
import com.monito.mobile.domain.usecase.EnsurePetUseCase
import com.monito.mobile.domain.usecase.GetPetUseCase
import com.monito.mobile.domain.usecase.GetStateMinutesUseCase
import com.monito.mobile.domain.usecase.UpdatePetUseCase
import com.monito.mobile.sync.WeatherSync
import com.monito.mobile.watch.WatchEventHandler
import com.monito.mobile.watch.WatchMessenger
import com.monito.mobile.watch.WatchStateHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Inyección de dependencias manual: un solo lugar donde se arma el grafo. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // data
    private val devicePreferences = DevicePreferences(appContext)
    val petRepository: PetRepository =
        PetRepositoryImpl(RetrofitProvider.monitoApi, devicePreferences)
    val weatherRepository: WeatherRepository =
        WeatherRepositoryImpl(RetrofitProvider.weatherApi)

    // domain
    val ensurePet = EnsurePetUseCase(petRepository)
    val getPet = GetPetUseCase(petRepository)
    val getStateMinutes = GetStateMinutesUseCase(petRepository)
    val updatePet = UpdatePetUseCase(petRepository)

    // reloj
    val watchMessenger = WatchMessenger(appContext)
    val watchState = WatchStateHolder()
    val watchEventHandler = WatchEventHandler(petRepository, watchState, appScope)

    // sincronización periódica del clima hacia el reloj
    val weatherSync = WeatherSync(weatherRepository, watchMessenger)
}
