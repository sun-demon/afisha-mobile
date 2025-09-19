package ru.variiix.afisha

import ru.variiix.afisha.views.NavigationView
import ru.variiix.afisha.fragments.ExploreFragment
import ru.variiix.afisha.fragments.MyTicketsFragment
import ru.variiix.afisha.fragments.ProfileFragment
import ru.variiix.afisha.fragments.SavedFragment

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
        showFragment(ExploreFragment.newInstance())
    }

    private fun setupNavigation(navigationView: NavigationView) {
        navigationView.setOnNavigationItemSelectedListener { itemId ->
            when (itemId) {
                R.id.navigation_explore -> showFragment(ExploreFragment.newInstance())
                R.id.navigation_saved -> showFragment(SavedFragment.newInstance())
                R.id.navigation_my_tickets -> showFragment(MyTicketsFragment.newInstance())
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