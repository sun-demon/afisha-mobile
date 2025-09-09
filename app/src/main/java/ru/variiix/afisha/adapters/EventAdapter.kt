package ru.variiix.afisha.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import ru.variiix.afisha.R
import ru.variiix.afisha.models.Event
import androidx.core.graphics.toColorInt

class EventAdapter(private val events: List<Event>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView.findViewById(R.id.root)
        val title: TextView = itemView.findViewById(R.id.title_view)
        val image: ImageView = itemView.findViewById(R.id.image_view)
        val details: TextView = itemView.findViewById(R.id.details_view)
        val price: TextView = itemView.findViewById(R.id.price_view)
        val rating: TextView = itemView.findViewById(R.id.rating_view)
        val favorite: ImageView = itemView.findViewById(R.id.favorite_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        holder.title.text = event.title
        holder.details.text = event.details

        if (event.price != null) holder.price.text = event.price else holder.price.visibility = View.GONE

        if (event.rating != null) {
            holder.rating.text = event.rating.toString()
            val color = when (event.rating) {
                in 8.0f .. 10.0f -> "#4CAF50".toColorInt()
                in 6.0f .. 7.9f -> "#FFC107".toColorInt()
                in 4.0f .. 5.9f -> "#FF9800".toColorInt()
                else -> "#F44336".toColorInt()
            }
            holder.rating.backgroundTintList = ColorStateList.valueOf(color)
        } else holder.rating.visibility = View.GONE

        holder.image.load(event.imageUrl) {
            placeholder(R.drawable.icon_placeholder_gray)
            error(R.drawable.icon_placeholder_gray)
            transformations(RoundedCornersTransformation(32f))
        }

        event.id
        holder.favorite.isSelected = false // but must be other
        holder.favorite.setOnClickListener {
            it.isSelected = !it.isSelected
//            if (it.isSelected)
                // add the event to favorites
        }

//        holder.root.setOnClickListener {
//
//        }
    }
}
