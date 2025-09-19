package ru.variiix.afisha.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import ru.variiix.afisha.R
import ru.variiix.afisha.models.Event
import androidx.core.graphics.toColorInt
import ru.variiix.afisha.utils.LocalFavorites
import ru.variiix.afisha.utils.UserSession

class EventAdapter(
    private val onEventClick: (event: Event) -> Unit,
    private val onFavoriteClick: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView.findViewById(R.id.root)
        val title: TextView = itemView.findViewById(R.id.title_view)
        val image: ImageView = itemView.findViewById(R.id.image_view)
        val details: TextView = itemView.findViewById(R.id.details_view)
        val price: TextView = itemView.findViewById(R.id.price_view)
        val rating: TextView = itemView.findViewById(R.id.rating_view)
        val favorite: ImageButton = itemView.findViewById(R.id.favorite_button)
        val ticket: ImageView = itemView.findViewById(R.id.ticket_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)

        // title and details
        holder.title.text = event.title
        holder.details.text = event.details

        // price
        if (event.price != null) {
            holder.price.visibility = View.VISIBLE
            holder.price.text = event.price
        } else {
            holder.price.visibility = View.GONE
        }

        // rating
        if (event.rating != null) {
            holder.rating.visibility = View.VISIBLE
            holder.rating.text = event.rating.toString()
            val color = when (event.rating) {
                in 8.0f..10.0f -> "#4CAF50".toColorInt()
                in 6.0f..7.9f -> "#FFC107".toColorInt()
                in 4.0f..5.9f -> "#FF9800".toColorInt()
                else -> "#F44336".toColorInt()
            }
            holder.rating.backgroundTintList = ColorStateList.valueOf(color)
        } else {
            holder.rating.visibility = View.GONE
        }

        // load image
        holder.image.load(event.imageUrl) {
            placeholder(R.drawable.icon_placeholder_gray)
            error(R.drawable.icon_placeholder_gray)
            transformations(RoundedCornersTransformation(32f))
        }

        // favorite button or ticket image view
        if (event.isTicket) {
            holder.favorite.visibility = View.GONE
            holder.ticket.visibility = View.VISIBLE
        } else {
            holder.favorite.visibility = View.VISIBLE
            holder.ticket.visibility = View.GONE
            holder.favorite.isSelected = event.isFavorite or LocalFavorites.contains(event.id)
            // favorite click
            holder.favorite.setOnClickListener {
                holder.root.requestDisallowInterceptTouchEvent(true)
                it.isSelected = !it.isSelected
                onFavoriteClick(event)
            }
        }

        // event click
        holder.root.setOnClickListener {
            onEventClick(event)
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem == newItem
    }
}
