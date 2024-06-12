package com.dicoding.storyapp.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.regex.Pattern

class CustomEmailText : AppCompatEditText {
    private var textInputLayout: TextInputLayout? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init()
    }

    fun setTextInputLayout(layout: TextInputLayout) {
        textInputLayout = layout
        init()
    }

    private fun init() {
        textInputLayout?.let { layout ->
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!isValidEmail(s)) {
                        layout.error = "Email tidak valid"
                    } else {
                        layout.error = null
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return if(target == null) {
            false
        } else {
            val emailPattern = Pattern.compile(
                "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            )
            emailPattern.matcher(target).matches()
        }
    }

    fun isValid(): Boolean {
        return isValidEmail(text)
    }

}