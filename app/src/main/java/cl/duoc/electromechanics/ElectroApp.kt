package cl.duoc.electromechanics

import android.app.Application
import cl.duoc.electromechanics.di.AppContainer

class ElectroApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notificationHelper.createChannel()
    }
}
