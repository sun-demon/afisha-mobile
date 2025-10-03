package ru.variiix.afisha.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import ru.variiix.afisha.MainActivity
import ru.variiix.afisha.R
import ru.variiix.afisha.adapters.EventAdapter
import ru.variiix.afisha.databinding.FragmentEventsBinding
import ru.variiix.afisha.models.Event
import ru.variiix.afisha.network.ApiClient
import ru.variiix.afisha.utils.LocalFavorites
import ru.variiix.afisha.utils.UserSession
import kotlin.coroutines.cancellation.CancellationException

class EventsFragment : Fragment() {
    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EventAdapter(
            { onEventClick(it) },
            { onFavoriteClick(it) }
        )

        val layoutManager = LinearLayoutManager(requireContext())
        binding.eventsView.layoutManager = layoutManager
        binding.eventsView.adapter = adapter

        binding.rubricSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val rubric = resources.getStringArray(R.array.rubrics_query)[position]
                if (rubric != currentRubric) {
                    currentRubric = rubric
                    resetAndLoad()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.eventsView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
            viewLifecycleOwner.lifecycleScope.launch {
                val token = UserSession.getToken() ?: return@launch
                val isFavorite = LocalFavorites.contains(event.id)

                try {
                    if (isFavorite) {
                        removeFavoriteFromServer(event.id, token)
                        LocalFavorites.remove(event.id)
                    } else {
                        addFavoriteOnServer(event.id, token)
                        LocalFavorites.add(event.id)
                    }
                } catch (e: CancellationException) {
                    Log.w(EventsFragment::class.java.simpleName, e.message.toString())
                } catch (e: Exception) {
                    Log.e("Favorites", "Failed to toggle favorite", e)
                }
            }
        } else {
            if (LocalFavorites.contains(event.id)) {
                LocalFavorites.remove(event.id)
            } else {
                LocalFavorites.add(event.id)
            }
        }
    }

    suspend fun addFavoriteOnServer(eventId: String, token: String) {
        ApiClient.favoritesApi.addFavorite(eventId, "Bearer $token")
    }

    suspend fun removeFavoriteFromServer(eventId: String, token: String) {
        ApiClient.favoritesApi.removeFavorite(eventId, "Bearer $token")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showBuyTicketDialog(event: Event) {
        AlertDialog.Builder(requireContext())
            .setTitle("Купить билет")
            .setMessage("Хотите купить билет на \"${event.title}\"?")
            .setPositiveButton("Купить") { dialog, _ ->
                lifecycleScope.launch {
                    val token = UserSession.getToken() ?: return@launch
                    try {
                        ApiClient.ticketsApi.addTicket(event.id, "Bearer $token")
                        Toast.makeText(requireContext(), "Билет куплен", Toast.LENGTH_SHORT).show()
                        event.isTicket = true
                        adapter.notifyDataSetChanged()
                    } catch (_: Exception) {
                        Toast.makeText(requireContext(), "Не удалось купить билет", Toast.LENGTH_SHORT).show()
                    }
                }
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
        binding.messageView.visibility = View.GONE
        binding.eventsView.visibility = View.VISIBLE
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
        if (adapter.currentList.isEmpty()) {
            binding.progressBar.visibility = View.VISIBLE
        }
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val newEvents = fetchEventsFromServer(currentRubric, offset, limit)
                if (newEvents.isEmpty()) {
                    if (adapter.currentList.isEmpty()) {
                        showMessage("События не найдены")
                    }
                    hasMore = false
                } else {
                    binding.messageView.visibility = View.GONE
                    binding.eventsView.visibility = View.VISIBLE
                    val updatedList = adapter.currentList.toMutableList()
                    updatedList.addAll(newEvents)
                    adapter.submitList(updatedList)
                    offset += newEvents.size
                }
            } catch (e: CancellationException) {
                Log.w(EventsFragment::class.java.simpleName, e.message.toString())
            } catch (e: Exception) {
                if (adapter.currentList.isEmpty()) {
                    showMessage("Не удалось установить соединение с сервером")
                }
                Log.e("ExploreFragment", "Error loading events", e)
            } finally {
                isLoading = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private suspend fun fetchEventsFromServer(
        rubric: String,
        offset: Int,
        limit: Int
    ): List<Event> {
        val response = ApiClient.eventsApi.getEvents(
            rubric.takeIf { it != "all" },
            offset,
            limit,
            token = "Bearer ${UserSession.getToken()}"
        )
        return response.events
    }

    private fun showMessage(message: String) {
        binding.messageView.text = message
        binding.messageView.visibility = View.VISIBLE
        binding.eventsView.visibility = View.GONE
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object {
        fun newInstance() = EventsFragment()
    }
}
