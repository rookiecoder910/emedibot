sealed class Screen(val route: String) {


    object Main : Screen("main") {
        object intro:Screen("IntroScreen")
        object Home : Screen("home")
        object Schedule : Screen("schedule")
        object Settings : Screen("settings")

    }
    object Login : Screen("login")
}
