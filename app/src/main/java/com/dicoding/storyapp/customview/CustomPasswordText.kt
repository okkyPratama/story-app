package com.dicoding.storyapp.customview

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import com.dicoding.storyapp.R
import com.google.android.material.textfield.TextInputLayout

class CustomPasswordText : AppCompatEditText {
    private lateinit var textInputLayout: TextInputLayout

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
    }

    private fun init() {
        transformationMethod = PasswordTransformationMethod.getInstance()

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::textInputLayout.isInitialized) {
                    if (s?.length!! < 8) {
                        textInputLayout.error = context.getString(R.string.password_error_message)
                    } else {
                        textInputLayout.error = null
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    transformationMethod = HideReturnsTransformationMethod.getInstance()
                    return@setOnTouchListener false
                }
                MotionEvent.ACTION_UP -> {
                    transformationMethod = PasswordTransformationMethod.getInstance()
                    return@setOnTouchListener false
                }
            }
            false
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    fun isValid(): Boolean {
        return text?.length!! >= 8
    }

}