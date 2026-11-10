package com.ruimeng.things.home.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import com.ruimeng.things.R
import wongxd.common.loadImg

class HomeFunctionZoneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val state = HomeFunctionZoneState()
    private var functionClickListener: ((HomeFunctionAction) -> Unit)? = null

    init {
        orientation = VERTICAL
        render()
    }

    fun setPackageFrozen(frozen: Boolean) {
        state.setPackageFrozen(frozen)
        render()
    }

    fun setOnFunctionClickListener(listener: (HomeFunctionAction) -> Unit) {
        functionClickListener = listener
    }

    private fun render() {
        removeAllViews()
        state.items().chunked(COLUMN_COUNT).forEach { rowItems ->
            val row = LinearLayout(context).apply {
                orientation = HORIZONTAL
                weightSum = COLUMN_COUNT.toFloat()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            rowItems.forEach { item ->
                row.addView(createItemView(row, item))
            }
            repeat(COLUMN_COUNT - rowItems.size) {
                row.addView(
                    Space(context),
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                )
            }
            addView(row)
        }
        requestLayout()
    }

    private fun createItemView(row: LinearLayout, item: HomeFunctionItem): View {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.item_home_function, row, false)
        val iconView = itemView.findViewById<ImageView>(R.id.iv_home_function_icon)
        val titleView = itemView.findViewById<TextView>(R.id.tv_home_function_title)

        iconView.loadImg(homeFunctionIconUrl(item.iconName))
        titleView.text = item.title
        itemView.contentDescription = item.title
        itemView.setOnClickListener {
            if (item.control != null) {
                state.toggleExpanded()
                render()
            } else {
                item.action?.let { action -> functionClickListener?.invoke(action) }
            }
        }
        return itemView
    }

    companion object {
        private const val COLUMN_COUNT = 5
    }
}
