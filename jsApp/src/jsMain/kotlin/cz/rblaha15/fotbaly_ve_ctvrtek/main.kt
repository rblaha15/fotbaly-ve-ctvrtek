package cz.rblaha15.fotbaly_ve_ctvrtek

import io.kvision.Application
import io.kvision.BootstrapModule
import io.kvision.html.p
import io.kvision.module
import io.kvision.panel.root
import io.kvision.routing.Routing
import io.kvision.startApplication
import io.kvision.theme.Theme
import io.kvision.theme.ThemeManager


fun main() {
    startApplication(
        ::App,
        module.hot,
        BootstrapModule,
    )
}

class App : Application() {

    init {
        ThemeManager.init(initialTheme = Theme.DARK, remember = false)
    }

    private val repository = createRepository()
    private val viewModel = ManViewModel(repository)

    override fun start() {
        root("kvapp") {
            Routing.init(useHash = false)
                .on("/reset", {
                    val p = p()
                    viewModel.clearAll {
                        p.content = "Ok"
                    }
                })
                .on({
                    mainContent(viewModel)
                })
        }
    }
}