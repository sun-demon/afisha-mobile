package ru.variiix.afisha.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import ru.variiix.afisha.R

class EventTypeSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.spinnerStyle
) : AppCompatSpinner(context, attrs, defStyleAttr) {

    init {
        gravity = Gravity.CENTER
        background = ContextCompat.getDrawable(context, R.drawable.background_spinner)
        setPadding(3.dpToPx(), 3.dpToPx(), 20.dpToPx(), 3.dpToPx())

        val items = context.resources.getStringArray(R.array.rubrics)
        adapter = ArrayAdapter(
            context,
            R.layout.spinner_item,
            items
        ).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
        setSelection(0)
    }


//    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
//        background = ContextCompat.getDrawable(
//            context,
//            if (gainFocus) R.drawable.background_spinner else R.drawable.background_spinner_selected
//        )
//        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
//    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
