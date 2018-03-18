package com.example.dan.stepikapp

import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.example.dan.stepikapp.adapter.CourseListAdapter
import com.example.dan.stepikapp.commons.InfiniteScrollListener
import com.example.dan.stepikapp.commons.Model
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: CourseListAdapter
    private var disposable: Disposable? = null
    private var coursesList: MutableList<Model.Course> = mutableListOf()
    private var featuredList: MutableList<Model.Course> = mutableListOf()
    private var page: Int = 1
    private var prevString: String = ""

    private val lm = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
    private val searchMode   = "search"
    private val featuredMode = "featured"
    private val aboutMode    = "about"


    private val stepikApiServe by lazy {
        StepikApiService.create()
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_search -> {
                message.setText(R.string.title_search)
                hideViews(searchMode)
                adapter.courseList = coursesList
                adapter.state = searchMode
                adapter.notifyDataSetChanged()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_featured -> {
                message.setText(R.string.title_featured)
                hideViews(featuredMode)
                adapter.courseList = featuredList
                adapter.state = featuredMode
                adapter.notifyDataSetChanged()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_about -> {
                message.setText(R.string.title_info)
                hideViews(aboutMode)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_search.setOnClickListener {
                val searchString: String = edit_search.text.toString()
                if (!searchString.equals(prevString)) {
                    page = 1
                    this.coursesList.clear()
                }
                beginSearch(page, searchString)
        }
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        rv_courses_list.layoutManager = lm
        rv_courses_list.addOnScrollListener(InfiniteScrollListener(
                                                { beginSearch(page++, prevString) }, lm))

        adapter = CourseListAdapter(searchMode) { course: Model.Course, position: Int ->
                                                        updateLists(course, position) }
        adapter.courseList = coursesList
        rv_courses_list.adapter = adapter

        beginSearch(page++, prevString)
    }


    private fun setFlags(where: List<Model.Course>, optionList: List<Model.Course>, flag: Boolean ) {
        where.forEach { x -> if (optionList.any{y -> y.course == x.course}) {x.isFeatured = !flag} else {x.isFeatured = flag} }
    }


    private fun loadMore(result: Model.Result) {
        coursesList.addAll(result.search_results)
        setFlags(coursesList, featuredList, false)
        adapter.courseList = coursesList
        adapter.notifyDataSetChanged()
    }


    private fun beginSearch(curPage: Int, searchString: String) {
        prevString = searchString
        disposable = stepikApiServe.findCourses(curPage, searchString)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result -> loadMore(result)},
                        { error -> Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show() }
                )
    }

    private fun hideViews(param: String) {
        if (param.equals(searchMode)) {
            edit_search.setVisibility(View.VISIBLE)
            btn_search.setVisibility(View.VISIBLE)
            rv_courses_list.setVisibility(View.VISIBLE)
        }
        if (param.equals(featuredMode)) {
            edit_search.setVisibility(View.GONE)
            btn_search.setVisibility(View.GONE)
            rv_courses_list.setVisibility(View.VISIBLE)
        }
        if (param.equals(aboutMode)) {
            edit_search.setVisibility(View.GONE)
            btn_search.setVisibility(View.GONE)
            rv_courses_list.setVisibility(View.GONE)
        }
    }

    private fun updateLists(course: Model.Course, position: Int) {
        if (adapter.state.equals(searchMode)) {
            if (!coursesList[position].isFeatured) {
                coursesList[position].isFeatured = true
                featuredList.add(course)
            } else {
                coursesList[position].isFeatured = false
                featuredList.removeAll { it.course == course.course }
                setFlags(featuredList, coursesList, false)
            }
        }
        if (adapter.state.equals(featuredMode)) {
            if (course.isFeatured == true) {
                featuredList.remove(course)
                adapter.courseList = featuredList
                adapter.notifyDataSetChanged()
                setFlags(coursesList, featuredList, false)
            } else {
                featuredList.add(course)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }
}
