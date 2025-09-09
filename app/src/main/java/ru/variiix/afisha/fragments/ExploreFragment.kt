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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.variiix.afisha.R
import ru.variiix.afisha.adapters.EventAdapter
import ru.variiix.afisha.models.Event
import ru.variiix.afisha.views.EventTypeSpinner
import java.io.IOException

class ExploreFragment : Fragment() {
    private lateinit var rubricView: EventTypeSpinner
    private lateinit var messageView: TextView
    private lateinit var eventsView: RecyclerView

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

        rubricView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val rubric = resources.getStringArray(R.array.rubrics_query)[position]
                loadEvents(rubric)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        eventsView.layoutManager = LinearLayoutManager(requireContext())

        loadEvents()
    }

    private fun loadEvents(rubric: String = "all") {
        if (!isNetworkAvailable(requireContext())) {
            showMessage("Нет подключения к интернету")
            return
        }

        lifecycleScope.launch {
            try {
                val events = fetchEventsFromServer(rubric)
                if (events.isEmpty()) {
                    showMessage("События не найдены")
                } else {
                    messageView.visibility = View.GONE
                    eventsView.adapter = EventAdapter(events)
                }
            } catch (e: Exception) {
                showMessage("Не удалось установить соединение с сервером")
                Log.e("ExploreFragment", "Error loading events", e)
            }
        }
    }

    private suspend fun fetchEventsFromServer(rubric: String): List<Event> = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://afisha.ddns.net/api/events${if (rubric == "all") "" else "?rubric=$rubric" }")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val body = response.body.string()
            val gson = Gson()
            val listType = object : TypeToken<List<Event>>() {}.type
            gson.fromJson(body, listType)
        }
    }

    private fun showMessage(message: String) {
        messageView.text = message
        messageView.visibility = View.VISIBLE
        eventsView.adapter = null
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
