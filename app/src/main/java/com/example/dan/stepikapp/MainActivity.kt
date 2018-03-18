package com.example.dan.stepikapp

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_main.*
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import android.widget.Toast
import com.example.dan.stepikapp.adapter.CourseListAdapter
import com.example.dan.stepikapp.commons.InfiniteScrollListener
import com.example.dan.stepikapp.commons.Model

class MainActivity : AppCompatActivity() {
    private var disposable: Disposable? = null

    private lateinit var coursesList: MutableList<Model.Course>
    private lateinit var featuredList: MutableList<Model.Course>
    private lateinit var adapter: CourseListAdapter
    private var page: Int = 1
    private var prevString: String = ""
    private var state = "search"

    private val stepikApiServe by lazy {
        StepikApiService.create()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_search -> {
                message.setText(R.string.title_search)
                hideViews("search")
                adapter.courseList = coursesList
                adapter.state = "search"
                adapter.notifyDataSetChanged()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_featured -> {
                message.setText(R.string.title_featured)
                hideViews("featured")
                adapter.courseList = featuredList
                adapter.state = "featured"
                adapter.notifyDataSetChanged()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_about -> {
                message.setText(R.string.title_info)
                hideViews("about")
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

        beginSearch(page++, prevString)

        val rv = findViewById<RecyclerView>(R.id.courses_list)
        val lm = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rv.layoutManager = lm
        rv.addOnScrollListener(
                InfiniteScrollListener({ beginSearch(page++, prevString) }, lm)
        )

        featuredList = mutableListOf()
        coursesList = mutableListOf()
        adapter = CourseListAdapter(state) { course: Model.Course, position: Int -> kotlin.run{
                        if (adapter.state.equals("search")) {
                            if (!coursesList[position].isFeatured) {
                                coursesList[position].isFeatured = true
                                featuredList.add(course)
                            } else {
                                coursesList[position].isFeatured = false
                                featuredList.removeAll { it.course == course.course }
                                setFlags(featuredList, coursesList, false)
                            }
                        }
                        if (adapter.state.equals("featured")) {
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
        }
        adapter.courseList = coursesList
        rv.adapter = adapter
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
        if (param.equals("search")) {
            edit_search.setVisibility(View.VISIBLE)
            btn_search.setVisibility(View.VISIBLE)
            courses_list.setVisibility(View.VISIBLE)
        }
        if (param.equals("featured")) {
            edit_search.setVisibility(View.GONE)
            btn_search.setVisibility(View.GONE)
            courses_list.setVisibility(View.VISIBLE)
        }
        if (param.equals("about")) {
            edit_search.setVisibility(View.GONE)
            btn_search.setVisibility(View.GONE)
            courses_list.setVisibility(View.GONE)

        }

    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }
}
