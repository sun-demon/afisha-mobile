package ru.variiix.afisha.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import ru.variiix.afisha.R
import ru.variiix.afisha.databinding.FragmentProfileBinding
import ru.variiix.afisha.network.ApiClient
import ru.variiix.afisha.utils.LocalFavorites
import ru.variiix.afisha.utils.UserSession
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var avatarUri: Uri? = null
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            avatarUri = it
            binding.avatarImage.setImageURI(it)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (UserSession.isAuthorized()) {
            updateUI(ProfileState.PROFILE)
        } else {
            updateUI(ProfileState.LOGIN)
        }

        binding.loginButton.setOnClickListener { onLoginButtonClickListener() }

        binding.registerButton.setOnClickListener { onRegisterButtonClickListener() }


        binding.logoutButton.setOnClickListener {
            UserSession.clear()
            LocalFavorites.clear()
            clearLoginForm()
            clearRegisterForm()
            updateUI(ProfileState.LOGIN)
        }

        binding.toRegisterText.setOnClickListener { updateUI(ProfileState.REGISTER) }
        binding.toLoginText.setOnClickListener { updateUI(ProfileState.LOGIN) }

        binding.editAvatarButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        setupLoginFormListeners()
        setupRegisterFormListeners()
    }

    private fun clearLoginForm() {
        binding.loginInput.setText("")
        binding.passwordLogin.setText("")
        binding.loginInputLayout.error = null
        binding.passwordLoginLayout.error = null
        binding.loginFormError.visibility = View.GONE
    }

    private fun clearRegisterForm() {
        binding.nameRegister.setText("")
        binding.emailRegister.setText("")
        binding.passwordRegister.setText("")
        binding.passwordRegisterRepeat.setText("")
        binding.nameRegisterLayout.error = null
        binding.emailRegisterLayout.error = null
        binding.passwordRegisterLayout.error = null
        binding.passwordRegisterRepeatLayout.error = null
        binding.registerFormError.visibility = View.GONE
        binding.avatarImage.setImageResource(R.drawable.icon_avatar)
    }


    @SuppressLint("SetTextI18n")
    private fun onLoginButtonClickListener() {
        hideKeyboard()
        val login = binding.loginInput.text.toString().trim() // one field for email or username
        val password = binding.passwordLogin.text.toString().trim()

        var hasError = false

        if (login.isEmpty()) {
            binding.loginInputLayout.error = "Введите логин или email"
            hasError = true
        } else if (login.contains("@") && !android.util.Patterns.EMAIL_ADDRESS.matcher(login).matches()) {
            binding.loginInputLayout.error = "Некорректный email"
            hasError = true
        } else {
            binding.loginInputLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordLoginLayout.error = "Введите пароль"
            hasError = true
        } else {
            binding.passwordLoginLayout.error = null
        }

        if (hasError) return

        binding.loginFormError.visibility = View.GONE
        binding.loginFormError.text = ""

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.authApi.login(login, password)

                UserSession.saveToken(response.accessToken)
                UserSession.saveUser(response.user)
                updateUI(ProfileState.PROFILE)
            } catch (e: CancellationException) {
                Log.w(ProfileFragment::class.java.simpleName, e.message.toString())
            } catch (_: IOException) {
                binding.loginFormError.visibility = View.VISIBLE
                binding.loginFormError.text = "Сервер недоступен. Проверьте интернет"
            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> {
                        binding.loginFormError.visibility = View.VISIBLE
                        binding.loginFormError.text = "Некорректные данные"
                    }
                    401 -> {
                        binding.passwordLoginLayout.error = "Неверный логин или пароль"
                    }
                    else -> {
                        binding.loginFormError.visibility = View.VISIBLE
                        binding.loginFormError.text = "Ошибка сервера: ${e.code()}"
                    }
                }
            } catch (_: Exception) {
                binding.loginFormError.visibility = View.VISIBLE
                binding.loginFormError.text = "Неизвестная ошибка"
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun onRegisterButtonClickListener() {
        hideKeyboard()
        val username = binding.nameRegister.text.toString().trim()
        val email = binding.emailRegister.text.toString().trim()
        val password = binding.passwordRegister.text.toString().trim()
        val repeatPassword = binding.passwordRegisterRepeat.text.toString().trim()

        var hasError = false

        if (username.isEmpty()) {
            binding.nameRegisterLayout.error = "Введите имя"
            hasError = true
        } else if (username.length < 3 || !username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            binding.nameRegisterLayout.error = "Логин должен быть ≥3 символов, только буквы, цифры и _"
            hasError = true
        } else {
            binding.nameRegisterLayout.error = null
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailRegisterLayout.error = "Неверный email"
            hasError = true
        } else {
            binding.emailRegisterLayout.error = null
        }

        if (password.length < 6) {
            binding.passwordRegisterLayout.error = "Пароль должен быть ≥ 6 символов"
            hasError = true
        } else {
            binding.passwordRegisterLayout.error = null
        }

        if (password != repeatPassword) {
            binding.passwordRegisterRepeatLayout.error = "Пароли не совпадают"
            hasError = true
        } else {
            binding.passwordRegisterRepeatLayout.error = null
        }

        if (hasError) return

        binding.registerFormError.visibility = View.GONE
        binding.registerFormError.text = ""

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val avatarPart = avatarUri?.let { uri ->
                    val stream = requireContext().contentResolver.openInputStream(uri)!!
                    val bytes = stream.readBytes()
                    stream.close()

                    val requestFile = bytes.toRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestFile)
                }

                val response = ApiClient.authApi.register(
                    username.toRequestBody("text/plain".toMediaType()),
                    password.toRequestBody("text/plain".toMediaType()),
                    email.toRequestBody("text/plain".toMediaType()),
                    avatarPart
                )
                UserSession.saveToken(response.accessToken)
                UserSession.saveUser(response.user)
                updateUI(ProfileState.PROFILE)
            } catch (e: CancellationException) {
                Log.w(ProfileFragment::class.java.simpleName, e.message.toString())
            } catch (_: IOException) {
                // network error
                binding.registerFormError.visibility = View.VISIBLE
                binding.registerFormError.text = "Сервер недоступен. Проверьте интернет соединение"
            } catch (e: HttpException) {
                // server error
                when (e.code()) {
                    400 -> {
                        binding.registerFormError.visibility = View.VISIBLE
                        binding.registerFormError.text = "Некорректные данные"
                    }
                    409 -> {
                        val body = e.response()?.errorBody()?.string() ?: ""
                        if (body.contains("Email")) {
                            binding.emailRegisterLayout.error = "Email уже используется"
                        }
                        if (body.contains("Username")) {
                            binding.nameRegisterLayout.error = "Логин уже используется"
                        }
                    }
                    else -> {
                        binding.registerFormError.visibility = View.VISIBLE
                        binding.registerFormError.text = "Ошибка сервера: ${e.code()}"
                    }
                }
            } catch (_: Exception) {
                binding.registerFormError.visibility = View.VISIBLE
                binding.registerFormError.text = "Неизвестная ошибка"
            }
        }
    }

    @SuppressLint("ServiceCast")
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun setupLoginFormListeners() {
        val fields = listOf(
            binding.loginInputLayout,
            binding.passwordLoginLayout
        )

        val editTexts = listOf(
            binding.loginInput,
            binding.passwordLogin
        )

        editTexts.forEachIndexed { index, editText ->
            editText.addTextChangedListener {
                fields[index].error = null
                binding.loginFormError.visibility = View.GONE
            }
        }
    }

    private fun setupRegisterFormListeners() {
        val fields = listOf(
            binding.nameRegisterLayout,
            binding.emailRegisterLayout,
            binding.passwordRegisterLayout,
            binding.passwordRegisterRepeatLayout
        )

        val editTexts = listOf(
            binding.nameRegister,
            binding.emailRegister,
            binding.passwordRegister,
            binding.passwordRegisterRepeat
        )

        editTexts.forEachIndexed { index, editText ->
            editText.addTextChangedListener {
                fields[index].error = null
                binding.registerFormError.visibility = View.GONE
            }
        }
    }

    private enum class ProfileState { LOGIN, REGISTER, PROFILE }

    private fun updateUI(state: ProfileState) {
        when(state) {
            ProfileState.LOGIN -> {
                binding.authorizationForms.visibility = View.VISIBLE
                binding.loginForm.visibility = View.VISIBLE
                binding.titleView.text = "Вход"
                binding.registerForm.visibility = View.GONE
                binding.profileLayout.visibility = View.GONE
                clearLoginForm()
            }
            ProfileState.REGISTER -> {
                binding.authorizationForms.visibility = View.VISIBLE
                binding.registerForm.visibility = View.VISIBLE
                binding.titleView.text = "Регистрация"
                binding.loginForm.visibility = View.GONE
                binding.profileLayout.visibility = View.GONE
                clearRegisterForm()
            }
            ProfileState.PROFILE -> {
                binding.authorizationForms.visibility = View.GONE
                binding.profileLayout.visibility = View.VISIBLE
                binding.titleView.text = "Профиль"
                val user = UserSession.getUser()
                binding.nameView.text = user?.username ?: "Неизвестный"
                binding.emailView.text = user?.email ?: ""

                val avatarUrl = user?.avatarUrl
                if (avatarUrl != null) {
                    Glide.with(this)
                        .load("https://afisha.ddns.net/users/${user.id}/avatar")
                        .circleCrop() // round image
                        .into(binding.avatarView)
                } else {
                    binding.avatarView.setImageResource(R.drawable.icon_user_black)
                }
            }
        }
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
}
