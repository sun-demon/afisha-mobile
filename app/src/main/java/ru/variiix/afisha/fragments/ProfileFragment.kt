package ru.variiix.afisha.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import ru.variiix.afisha.R
import ru.variiix.afisha.utils.UserSession


class ProfileFragment : Fragment() {

    private lateinit var authorizationForms: View
    private lateinit var profileLayout: View
    private lateinit var loginForm: View
    private lateinit var registerForm: View

    private lateinit var avatarView: ImageView
    private lateinit var nameView: TextView
    private lateinit var emailView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authorizationForms = view.findViewById(R.id.authorization_forms)
        profileLayout = view.findViewById(R.id.profile_layout)
        loginForm = view.findViewById(R.id.login_form)
        registerForm = view.findViewById(R.id.register_form)

        avatarView = view.findViewById(R.id.avatar_view)
        nameView = view.findViewById(R.id.name_view)
        emailView = view.findViewById(R.id.email_view)

        val loginButton = view.findViewById<Button>(R.id.login_button)
        val registerButton = view.findViewById<Button>(R.id.register_button)
        val logoutButton = view.findViewById<Button>(R.id.logout_button)
        val toRegisterText = view.findViewById<TextView>(R.id.to_register_text)
        val toLoginText = view.findViewById<TextView>(R.id.to_login_text)
        val editAvatarButton = view.findViewById<ImageButton>(R.id.edit_avatar_button)

        if (UserSession.isAuthorized()) {
            showProfile()
        } else {
            showLoginForm()
        }

        loginButton.setOnClickListener {
            // TODO: login
        }

        registerButton.setOnClickListener {
            // TODO: registration
        }

        logoutButton.setOnClickListener {
            UserSession.clear()
            showLoginForm()
        }

        toRegisterText.setOnClickListener {
            showRegisterForm()
        }

        toLoginText.setOnClickListener {
            showLoginForm()
        }

        editAvatarButton.setOnClickListener {
            Toast.makeText(requireContext(), "Выбор аватара (TODO)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProfile() {
        authorizationForms.visibility = View.GONE
        profileLayout.visibility = View.VISIBLE
        val user = UserSession.getUser()
        nameView.text = user?.name ?: "Неизвестный"
        emailView.text = user?.email ?: ""
    }

    private fun showLoginForm() {
        authorizationForms.visibility = View.VISIBLE
        profileLayout.visibility = View.GONE
        loginForm.visibility = View.VISIBLE
        registerForm.visibility = View.GONE
    }

    private fun showRegisterForm() {
        authorizationForms.visibility = View.VISIBLE
        profileLayout.visibility = View.GONE
        loginForm.visibility = View.GONE
        registerForm.visibility = View.VISIBLE
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
}
