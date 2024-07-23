package com.pkmkcub.spectragrow.view.customview

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import androidx.appcompat.widget.AppCompatEditText
import com.pkmkcub.spectragrow.R

class Email : AppCompatEditText {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var isEmailValid = false
        private set

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                isEmailValid = Patterns.EMAIL_ADDRESS.matcher(input).matches()

                if (!isEmailValid) {
                    setBackgroundResource(R.drawable.rounded_button_error)
                    error = context.getString(R.string.incorrect_email_format)
                    setTextColor(Color.RED)
                } else {
                    setBackgroundResource(R.drawable.rounded_button)
                    error = null
                    setTextColor(Color.BLACK)
                }
            }
        })
    }
}
