package ru.variiix.afisha.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.variiix.afisha.MainActivity
import ru.variiix.afisha.R
import ru.variiix.afisha.adapters.EventAdapter
import ru.variiix.afisha.models.Event
import ru.variiix.afisha.models.EventsResponse
import ru.variiix.afisha.utils.LocalFavorites
import ru.variiix.afisha.utils.UserSession
import ru.variiix.afisha.views.EventTypeSpinner
import java.io.IOException

class ExploreFragment : Fragment() {
    private lateinit var rubricView: EventTypeSpinner
    private lateinit var messageView: TextView
    private lateinit var eventsView: RecyclerView

    private lateinit var adapter: EventAdapter

    private var currentRubric: String = "all"
    private var isLoading = false
    private var hasMore = true

    private var offset = 0
    private val limit = 12

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rubricView = view.findViewById(R.id.rubric_spinner)
        messageView = view.findViewById(R.id.message_view)
        eventsView = view.findViewById(R.id.events_view)

        // events adapter
        adapter = EventAdapter(
            { onEventClick(it) },
            { onFavoriteClick(it) }
        )

        val layoutManager = LinearLayoutManager(requireContext())
        eventsView.layoutManager = layoutManager
        eventsView.adapter = adapter

        // on rubric select
        rubricView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val rubric = resources.getStringArray(R.array.rubrics_query)[position]
                if (rubric != currentRubric) {
                    currentRubric = rubric
                    resetAndLoad()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // autoload on scroll
        eventsView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && !isLoading && hasMore) { // only to bottom scroll
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                    if (visibleItemCount + firstVisibleItem >= totalItemCount - 10) {
                        loadEvents()
                    }
                }
            }
        })

        resetAndLoad()
    }

    private fun onEventClick(event: Event) {
        if (UserSession.isAuthorized()) {
            if (event.isTicket) {
                Toast.makeText(requireContext(), "Билет уже куплен", Toast.LENGTH_SHORT).show()
            } else {
                showBuyTicketDialog(event)
            }
        } else {
            showLoginRequiredDialog()
        }
    }

    private fun onFavoriteClick(event: Event) {
        if (UserSession.isAuthorized()) {
            // user authorized - sync with server
            lifecycleScope.launch {
                val token = UserSession.getToken() ?: return@launch
                val isFavorite = LocalFavorites.contains(event.id)

                try {
                    if (isFavorite) {
                        removeFavoriteFromServer(event.id, token)
                        LocalFavorites.remove(event.id) // sync local
                    } else {
                        addFavoriteOnServer(event.id, token)
                        LocalFavorites.add(event.id) // sync local
                    }
                    // update ui
//                    adapter.notifyItemChanged(adapter.currentList.indexOf(event))
                } catch (e: Exception) {
                    Log.e("Favorites", "Failed to toggle favorite", e)
                }
            }
        } else {
            // not authorized — work only with local storage
            if (LocalFavorites.contains(event.id)) {
                LocalFavorites.remove(event.id)
            } else {
                LocalFavorites.add(event.id)
            }
//            adapter.notifyItemChanged(adapter.currentList.indexOf(event))
        }
    }

    suspend fun addFavoriteOnServer(eventId: String, token: String) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://yourserver.com/api/favorites/$eventId")
                .post("".toRequestBody()) // empty body
                .addHeader("Authorization", "Bearer $token")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Failed: ${response.code}")
        }
    }

    suspend fun removeFavoriteFromServer(eventId: String, token: String) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://yourserver.com/api/favorites/$eventId")
                .delete()
                .addHeader("Authorization", "Bearer $token")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Failed: ${response.code}")
        }
    }

    private fun showBuyTicketDialog(event: Event) {
        AlertDialog.Builder(requireContext())
            .setTitle("Купить билет")
            .setMessage("Хотите купить билет на \"${event.title}\"?")
            .setPositiveButton("Купить") { dialog, _ ->
                // здесь вызов API покупки билета
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showLoginRequiredDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Авторизация")
            .setMessage("Для покупки билета необходимо войти в аккаунт")
            .setPositiveButton("Войти") { _, _ ->
                (activity as? MainActivity)?.showFragment(ProfileFragment.newInstance())
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun resetAndLoad() {
        adapter.submitList(emptyList())
        offset = 0
        hasMore = true
        messageView.visibility = View.GONE
        eventsView.visibility = View.VISIBLE
        loadEvents()
    }

    private fun loadEvents() {
        if (!isNetworkAvailable(requireContext())) {
            showMessage("Нет подключения к интернету")
            return
        }
        if (isLoading || !hasMore) {
            return
        }

        isLoading = true
        lifecycleScope.launch {
            try {
                val newEvents = fetchEventsFromServer(currentRubric, offset, limit)
                if (newEvents.isEmpty()) {
                    if (adapter.currentList.isEmpty()) {
                        showMessage("События не найдены")
                    }
                    hasMore = false
                } else {
                    messageView.visibility = View.GONE
                    eventsView.visibility = View.VISIBLE
                    val updatedList = adapter.currentList.toMutableList()
                    updatedList.addAll(newEvents)
                    adapter.submitList(updatedList)
                    offset += newEvents.size
                }
            } catch (e: Exception) {
                if (adapter.currentList.isEmpty()) {
                    showMessage("Не удалось установить соединение с сервером")
                }
                Log.e("ExploreFragment", "Error loading events", e)
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun fetchEventsFromServer(
        rubric: String,
        offset: Int,
        limit: Int
    ): List<Event> = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val url = buildString {
            append("https://afisha.ddns.net/api/events")
            val params = mutableListOf<String>()
            if (rubric != "all") {
                params.add("rubric=$rubric")
            }
            params.add("offset=$offset")
            params.add("limit=$limit")
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
        }

        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }

            val body = response.body.string()
            val gson = Gson()

            val eventsResponse = gson.fromJson(body, EventsResponse::class.java)
            eventsResponse.events
        }
    }

    private fun showMessage(message: String) {
        messageView.text = message
        messageView.visibility = View.VISIBLE
        eventsView.visibility = View.GONE
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object {
        fun newInstance() = ExploreFragment()
    }
}
