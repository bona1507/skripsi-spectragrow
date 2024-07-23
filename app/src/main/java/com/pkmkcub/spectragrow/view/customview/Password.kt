package com.pkmkcub.spectragrow.view.customview

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.textfield.TextInputEditText
import com.pkmkcub.spectragrow.R

class Password : TextInputEditText {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var isPasswordVisible = false
    var isPasswordValid = false
        private set

    init {
        updateIcon()
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validatePassword(s.toString())
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val drawableEndIndex = 2
        if (event?.action == MotionEvent.ACTION_UP) {
            if (event.rawX >= (right - compoundDrawables[drawableEndIndex].bounds.width())) {
                togglePasswordVisibility()
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        transformationMethod = if (isPasswordVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }
        updateIcon()
        setSelection(text?.length ?: 0)
    }

    private fun updateIcon() {
        val drawable = if (isPasswordVisible) {
            R.drawable.visibility_on
        } else {
            R.drawable.visibility_off
        }
        setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawable, 0)
    }

    private fun validatePassword(password: String) {
        isPasswordValid = password.length >= 8
        when {
            !isPasswordValid -> {
                error = context.getString(R.string.incorrect_pw_format)
                setTextColor(Color.RED)
                setBackgroundResource(R.drawable.rounded_button_error)
            }
            else -> {
                error = null
                setTextColor(Color.BLACK)
                setBackgroundResource(R.drawable.rounded_button)
            }
        }
    }
}
