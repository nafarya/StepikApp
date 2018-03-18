package com.example.dan.stepikapp.commons

import com.google.gson.annotations.SerializedName

/**
 * Created by dan on 14.03.18.
 */
object Model {
    data class Result(
            @SerializedName("meta")val meta: Meta,
            @SerializedName("search-results") val search_results : List<Course>
    )

    data class Meta(
            val page: Int,
            val has_next: Boolean,
            val has_previous: Boolean
    )

    data class Course(
            val id: Int,
            val position: Int,

            val target_id: Int,
            val target_type: String,
            val course: Int,
            val course_owner: Int,
            val course_title: String,
            val course_slug: String,
            val course_cover: String,

            var isFeatured: Boolean

    )
}