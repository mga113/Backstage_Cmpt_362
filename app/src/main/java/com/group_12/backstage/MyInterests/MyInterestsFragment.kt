package com.group_12.backstage.MyInterests

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.group_12.backstage.R

class MyInterestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyInterestsAdapter
    private lateinit var emptyTextView: TextView
    private lateinit var searchView: SearchView
    private lateinit var chipGroup: ChipGroup
    private lateinit var progressBar: ProgressBar

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
        chipGroup = view.findViewById(R.id.filterChipGroup)
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        // Adapter with 2 callbacks: 
        // 1. Item Click (Navigation)
        // 2. Status Change (Firestore Update)
        adapter = MyInterestsAdapter(
            filteredList,
            onItemClick = { event, imageView ->
                navigateToEventDetails(event, imageView)
            },
            onStatusChange = { event, newStatus ->
                updateEventStatus(event, newStatus)
            }
        )
        recyclerView.adapter = adapter

        setupSearch()
        setupFilterChips()
        setupSwipeToDelete()
        listenForUserEvents()

        return view
    }

    private fun updateEventStatus(event: Event, newStatus: String) {
        val currentUser = auth.currentUser ?: return
        
        // Optimistic update isn't strictly necessary because the SnapshotListener 
        // will fire almost instantly, but we can verify the doc exists first.
        
        db.collection("users")
            .document(currentUser.uid)
            .collection("my_events")
            .document(event.id)
            .update("status", newStatus)
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToEventDetails(event: Event, imageView: ImageView) {
        val transitionName = "event_image_${event.id}"
        val extras = FragmentNavigatorExtras(imageView to transitionName)

        val bundle = bundleOf(
            "eventId" to event.id,
            "title" to event.title,
            "date" to event.date,
            "location" to event.location,
            "imageUrl" to event.imageUrl,
            "ticketUrl" to event.ticketUrl
        )

        findNavController().navigate(
            R.id.action_myInterests_to_eventDetails,
            bundle,
            null,
            extras
        )
    }

    private fun setupFilterChips() {
        chipGroup.setOnCheckedChangeListener { _, _ ->
            val query = searchView.query.toString().trim().lowercase()
            filterList(query)
        }
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val eventToDelete = filteredList[position]
                deleteEvent(eventToDelete, position)
            }
            
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                if (viewHolder != null) {
                    val foregroundView = viewHolder.itemView.findViewById<View>(R.id.view_foreground)
                    getDefaultUIUtil().onSelected(foregroundView)
                }
            }

            override fun onChildDrawOver(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder?,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                if (viewHolder != null) {
                    val foregroundView = viewHolder.itemView.findViewById<View>(R.id.view_foreground)
                    getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                val foregroundView = viewHolder.itemView.findViewById<View>(R.id.view_foreground)
                getDefaultUIUtil().clearView(foregroundView)
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val foregroundView = viewHolder.itemView.findViewById<View>(R.id.view_foreground)
                getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    private fun deleteEvent(event: Event, position: Int) {
        filteredList.removeAt(position)
        eventList.remove(event)
        adapter.notifyItemRemoved(position)
        
        if (filteredList.isEmpty()) emptyTextView.visibility = View.VISIBLE

        Snackbar.make(recyclerView, "${event.title} removed", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                filteredList.add(position, event)
                eventList.add(event)
                adapter.notifyItemInserted(position)
                emptyTextView.visibility = View.GONE
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, eventId: Int) {
                    if (eventId != DISMISS_EVENT_ACTION) {
                        permanentlyDeleteEvent(event)
                    }
                }
            })
            .show()
    }

    private fun permanentlyDeleteEvent(event: Event) {
        val currentUser = auth.currentUser ?: return
        if (event.id.isNotEmpty()) {
             db.collection("users").document(currentUser.uid)
                .collection("my_events").document(event.id)
                .delete()
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText?.trim()?.lowercase() ?: "")
                return true
            }
        })
    }

    private fun filterList(query: String) {
        val checkedChipId = chipGroup.checkedChipId
        val filterStatus = when (checkedChipId) {
            R.id.chipGoing -> "going"
            R.id.chipInterested -> "interested"
            else -> "all"
        }

        filteredList.clear()
        val searchFiltered = if (query.isEmpty()) eventList else eventList.filter {
            it.title.lowercase().contains(query) ||
            it.location.lowercase().contains(query) ||
            it.date.lowercase().contains(query)
        }

        val finalFiltered = if (filterStatus == "all") searchFiltered else searchFiltered.filter { 
            it.status.equals(filterStatus, ignoreCase = true) 
        }

        filteredList.addAll(finalFiltered)
        adapter.notifyDataSetChanged()
        emptyTextView.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun listenForUserEvents() {
        progressBar.visibility = View.VISIBLE 
        val currentUser = auth.currentUser
        if (currentUser == null) {
            loadDummyData()
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .collection("my_events")
            .addSnapshotListener { snapshots, e ->
                progressBar.visibility = View.GONE 
                
                if (e != null) {
                    Log.e("MyInterests", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    eventList.clear()
                    for (doc in snapshots) {
                        try {
                            val event = doc.toObject(Event::class.java).copy(id = doc.id)
                            eventList.add(event)
                        } catch (e: Exception) {
                            Log.e("MyInterests", "Parse error", e)
                        }
                    }
                    filterList(searchView.query.toString().trim().lowercase())
                }
            }
    }

    private fun loadDummyData() {
        // Only called if not logged in
        eventList.clear()
        eventList.add(Event("1", "Not Logged In", "N/A", "N/A", "", "interested"))
        filterList("")
        progressBar.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        searchView.clearFocus()
    }
}