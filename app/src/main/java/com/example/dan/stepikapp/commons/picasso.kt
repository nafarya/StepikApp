package com.example.dan.stepikapp.commons

import android.content.Context
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator

/**
 * Created by dan on 16.03.18.
 */

val Context.picasso: Picasso
    get() = Picasso.with(this)

fun ImageView.load(path: String?) {
    (context.picasso.load(path)).into(this)
}