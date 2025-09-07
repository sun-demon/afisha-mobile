package ru.variiix.afisha.views

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import ru.variiix.afisha.R
import androidx.core.content.withStyledAttributes


@SuppressLint("PrivateResource")
class NavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private lateinit var navigationItems: List<AppCompatTextView>
    private var lastSelectedItemIndex: Int = 0
    private var navigationListener: ((Int) -> Unit)? = null

    init {
        setupView()
        if (!isInEditMode) setupNavigation()
        selectItemWithoutAnimation(navigationItems[lastSelectedItemIndex])
    }

    private fun setupView() {
        LayoutInflater.from(context).inflate(R.layout.navigation_view, this, true)
        navigationItems = (get(0) as LinearLayout).children.filterIsInstance<AppCompatTextView>().toList()
    }

    private fun setupNavigation() {
        navigationItems.forEachIndexed { index, textView ->
            textView.setOnClickListener { selectItem(index) }
        }
    }

    fun selectItem(itemIndex: Int, notifyListener: Boolean = true) {
        if (lastSelectedItemIndex == itemIndex) return

        val newSelectedItem = navigationItems[itemIndex]
        updateItemAppearance(newSelectedItem, true)
        updateItemAppearance(navigationItems[lastSelectedItemIndex], false)

        lastSelectedItemIndex = itemIndex

        if (notifyListener) navigationListener?.invoke(newSelectedItem.id)
    }

    private fun updateItemAppearance(textView: TextView, isSelected: Boolean) {
        textView.isSelected = isSelected
        val scale = if (isSelected) 1.15f else 1.0f
        val scaleX = ObjectAnimator.ofFloat(textView, "scaleX", textView.scaleX, scale)
        val scaleY = ObjectAnimator.ofFloat(textView, "scaleY", textView.scaleY, scale)

        AnimatorSet().apply {
            duration = 200
            playTogether(listOfNotNull(scaleX, scaleY))
            start()
        }
    }

    private fun selectItemWithoutAnimation(textView: TextView) {
        textView.isSelected = true
        textView.scaleX = 1.15f
        textView.scaleY = 1.15f
    }

    fun setOnNavigationItemSelectedListener(listener: (Int) -> Unit) {
        navigationListener = listener
    }
//    fun getSelectedItemIndex(): Int = lastSelectedItemIndex
//    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}