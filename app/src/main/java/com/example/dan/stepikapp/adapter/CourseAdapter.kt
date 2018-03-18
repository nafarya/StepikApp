package com.example.dan.stepikapp.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.example.dan.stepikapp.R
import com.example.dan.stepikapp.commons.Model
import com.example.dan.stepikapp.commons.load

/**
 * Created by dan on 14.03.18.
 */

class CourseListAdapter(var state: String, val clickListener: (Model.Course, Int) -> Unit): RecyclerView.Adapter<CourseListAdapter.ViewHolder>() {

    lateinit var courseList: List<Model.Course>

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.setItem(courseList[position])
        holder?.checkbox?.setOnClickListener { clickListener(courseList[position], position) }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.course_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val img = itemView.findViewById<ImageView>(R.id.course_item_img)
        private val txtTitle = itemView.findViewById<TextView>(R.id.course_item_title)
        val checkbox = itemView.findViewById<CheckBox>(R.id.course_item_check_box)

        fun setItem(item: Model.Course) {
            img.load(item.course_cover)
            txtTitle.text = item.course_title
            checkbox.isChecked = item.isFeatured
        }
    }
}
