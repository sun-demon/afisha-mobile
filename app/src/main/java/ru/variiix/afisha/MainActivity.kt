package ru.variiix.afisha

import ru.variiix.afisha.views.NavigationView
import ru.variiix.afisha.fragments.EventsFragment
import ru.variiix.afisha.fragments.TicketsFragment
import ru.variiix.afisha.fragments.ProfileFragment
import ru.variiix.afisha.fragments.FavoritesFragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import ru.variiix.afisha.utils.LocalFavorites
import ru.variiix.afisha.utils.UserSession


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode((AppCompatDelegate.MODE_NIGHT_NO))

        super.onCreate(savedInstanceState)

        UserSession.init(this)
        LocalFavorites.init(this)

        setContentView(R.layout.activity_main)

        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        setupNavigation(navigationView)
        showFragment(EventsFragment.newInstance())
    }

    private fun setupNavigation(navigationView: NavigationView) {
        navigationView.setOnNavigationItemSelectedListener { itemId ->
            when (itemId) {
                R.id.navigation_events -> showFragment(EventsFragment.newInstance())
                R.id.navigation_favorites -> showFragment(FavoritesFragment.newInstance())
                R.id.navigation_tickets -> showFragment(TicketsFragment.newInstance())
                R.id.navigation_profile -> showFragment(ProfileFragment.newInstance())
            }
        }
    }

    fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}