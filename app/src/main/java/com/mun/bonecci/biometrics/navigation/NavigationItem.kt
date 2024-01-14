package com.mun.bonecci.biometrics.navigation

/**
 * Sealed class representing different screens in the navigation graph.
 *
 * @property route The unique route associated with each screen.
 */
sealed class NavigationItem(var route: String) {
    /**
     * Object representing Login Screen in the navigation graph.
     */
    data object LoginScreen : NavigationItem(route = LOGIN_SCREEN)

    /**
     * Object representing Result Screen in the navigation graph.
     */
    data object ResultScreen : NavigationItem(route = RESULT_SCREEN)

    companion object {
        const val LOGIN_SCREEN = "login_screen"
        const val RESULT_SCREEN = "result_screen"
    }
}