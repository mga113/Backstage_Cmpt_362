package com.group_12.backstage_group_12.MyInterests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.group_12.backstage_group_12.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyInterestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyInterestsAdapter
    private lateinit var emptyTextView: TextView
    private lateinit var searchView: SearchView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val eventList = mutableListOf<Event>()        // all events
    private val filteredList = mutableListOf<Event>()     // filtered events

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_interests, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewInterests)
        searchView = view.findViewById(R.id.searchBar)
        emptyTextView = view.findViewById(R.id.emptyTextView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MyInterestsAdapter(filteredList)
        recyclerView.adapter = adapter

        setupSearch()
        loadDummyInterests()

        return view
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.trim()?.lowercase() ?: ""
                filteredList.clear()

                if (query.isEmpty()) {
                    filteredList.addAll(eventList)
                } else {
                    filteredList.addAll(
                        eventList.filter {
                            it.title.lowercase().contains(query) ||
                                    it.location.lowercase().contains(query) ||
                                    it.date.lowercase().contains(query)
                        }
                    )
                }

                adapter.notifyDataSetChanged()
                emptyTextView.visibility =
                    if (filteredList.isEmpty()) View.VISIBLE else View.GONE

                return true
            }
        })
    }

    private fun loadDummyInterests() {
        eventList.clear()
        eventList.add(Event("Coldplay Concert", "BC Place Stadium", "2025-07-10"))
        eventList.add(Event("Drake Tour", "Rogers Arena", "2025-08-15"))
        eventList.add(Event("Taylor Swift Eras Tour", "BC Place", "2025-09-01"))
        eventList.add(Event("Jazz Festival", "Granville Island", "2025-06-20"))

        filteredList.clear()
        filteredList.addAll(eventList)
        adapter.notifyDataSetChanged()
        emptyTextView.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        searchView.clearFocus()
    }
}