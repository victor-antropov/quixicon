package com.quixicon.presentation.databinding

import android.util.TypedValue
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView
import com.quixicon.R
import com.quixicon.core.support.helpers.HtmlHelper

@BindingAdapter("android:textColor")
fun AppCompatTextView.getColor(colorId: Int) {
    val color = ContextCompat.getColor(context, colorId)
    this.setTextColor(color)
}

@BindingAdapter("html")
fun AppCompatTextView.getHtml(str: String) {
    HtmlHelper.setHtmlWithLinkClickHandler(this, str, context)
}

@BindingAdapter("strokeColor")
fun MaterialCardView.getStrokeColor(@ColorRes colorId: Int) {
    this.strokeColor = ContextCompat.getColor(context, colorId)
}

@BindingAdapter("strokeColor", requireAll = true)
fun MaterialCardView.getStrokeColorAttr(@AttrRes attrId: Int) {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attrId, typedValue, true)
    this.strokeColor = typedValue.data
}

@BindingAdapter("android:backgroundTint", requireAll = true)
fun FrameLayout.getBackgroundTintAttr(@AttrRes attrId: Int) {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attrId, typedValue, false)
    this.backgroundTintList = ContextCompat.getColorStateList(context, typedValue.data)
}

@BindingAdapter("knowledgeTint")
fun AppCompatImageView.getKnowledgeColor(value: Int?) {
    val colorId = when (value) {
        in 0 until 10 -> R.color.rotation1
        in 10 until 20 -> R.color.rotation2
        in 20 until 30 -> R.color.rotation3
        in 30 until 40 -> R.color.rotation4
        in 40 until 50 -> R.color.rotation5
        in 50 until 60 -> R.color.rotation6
        in 60 until 70 -> R.color.rotation7
        in 70 until 80 -> R.color.rotation8
        in 80 until 90 -> R.color.rotation9
        in 90..100 -> R.color.rotation10
        else -> R.color.gray_80
    }
    this.imageTintList = ContextCompat.getColorStateList(context, colorId)
}
