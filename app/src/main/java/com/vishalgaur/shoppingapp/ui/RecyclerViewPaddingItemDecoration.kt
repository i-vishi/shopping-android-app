package com.vishalgaur.shoppingapp.ui

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewPaddingItemDecoration(private val context: Context) :
    RecyclerView.ItemDecoration() {
    private val paddingSpace = 16

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(paddingSpace, paddingSpace, paddingSpace, paddingSpace)
    }
}