package com.group_12.backstage.MyInterests

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.location.Geocoder
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.group_12.backstage.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MyInterestsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyInterestsAdapter
    private lateinit var emptyTextView: TextView
    private lateinit var searchView: SearchView
    private lateinit var chipGroup: ChipGroup
    private lateinit var progressBar: ProgressBar
    private lateinit var mapView: MapView
    private lateinit var btnMapToggle: FloatingActionButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val eventList = mutableListOf<Event>()        // all events
    private val filteredList = mutableListOf<Event>()     // filtered events

    private var googleMap: GoogleMap? = null
    private var isMapView = false

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
        mapView = view.findViewById(R.id.mapView)
        btnMapToggle = view.findViewById(R.id.btnMapToggle)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        // Initialize MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
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
        setupMapToggle()
        setupSwipeRefresh()

        return view
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            // Re-apply filter to "refresh" the view
            val query = searchView.query.toString().trim().lowercase()
            filterList(query)
            
            // Optionally fetch location again
            fetchUserLocationAndMoveCamera()

            // Stop the refreshing animation
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Enable Zoom Controls & Compass
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isMapToolbarEnabled = false 

        // Add padding to move UI controls
        val density = resources.displayMetrics.density
        val paddingBottom = (180 * density).toInt()
        map.setPadding(0, 0, 0, paddingBottom)

        // Handle clicking on the "Info Window"
        map.setOnInfoWindowClickListener { marker ->
            val event = marker.tag as? Event
            if (event != null) {
                navigateToEventDetails(event, null)
            }
        }

        updateMapMarkers()
        fetchUserLocationAndMoveCamera()
    }

    private fun fetchUserLocationAndMoveCamera() {
        val currentUser = auth.currentUser ?: return
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val city = document.getString("myLocation") ?: "Vancouver, BC"
                    moveCameraToCity(city)
                }
            }
    }

    private fun moveCameraToCity(cityName: String) {
        val map = googleMap ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(cityName, 1)
                if (!addresses.isNullOrEmpty()) {
                    val location = addresses[0]
                    val latLng = LatLng(location.latitude, location.longitude)
                    withContext(Dispatchers.Main) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                    }
                }
            } catch (e: Exception) {
                Log.e("MyInterestsFragment", "Geocoding error", e)
            }
        }
    }

    private fun setupMapToggle() {
        btnMapToggle.setOnClickListener {
            isMapView = !isMapView
            if (isMapView) {
                // Hide SwipeRefreshLayout wrapper instead of just RecyclerView
                swipeRefreshLayout.visibility = View.GONE
                mapView.visibility = View.VISIBLE
                emptyTextView.visibility = View.GONE
                btnMapToggle.setImageResource(android.R.drawable.ic_menu_view) 
                updateMapMarkers()
            } else {
                swipeRefreshLayout.visibility = View.VISIBLE
                mapView.visibility = View.GONE
                btnMapToggle.setImageResource(android.R.drawable.ic_dialog_map) 
                emptyTextView.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateMapMarkers() {
        val map = googleMap ?: return
        map.clear()

        if (filteredList.isEmpty()) return

        for (event in filteredList) {
            if (event.latitude != 0.0 || event.longitude != 0.0) {
                addMarkerForEvent(map, event, LatLng(event.latitude, event.longitude))
            } else {
                geocodeAndAddMarker(map, event)
            }
        }
    }

    private fun geocodeAndAddMarker(map: GoogleMap, event: Event) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(event.location, 1)
                
                if (!addresses.isNullOrEmpty()) {
                    val location = addresses[0]
                    val latLng = LatLng(location.latitude, location.longitude)
                    withContext(Dispatchers.Main) {
                        addMarkerForEvent(map, event, latLng)
                    }
                }
            } catch (e: Exception) {
                Log.e("MyInterests", "Geocoding fallback error", e)
            }
        }
    }

    private fun addMarkerForEvent(map: GoogleMap, event: Event, position: LatLng) {
        try {
            Glide.with(requireContext())
                .asBitmap()
                .load(event.imageUrl)
                .circleCrop()
                .override(100, 100)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        try {
                            val marker = map.addMarker(
                                MarkerOptions()
                                    .position(position)
                                    .title(event.title)
                                    .snippet(event.location)
                                    .icon(BitmapDescriptorFactory.fromBitmap(getCircularBitmapWithBorder(resource)))
                            )
                            marker?.tag = event 
                        } catch (e: Exception) {
                            Log.e("MapMarker", "Error adding marker", e)
                        }
                    }
                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                })
        } catch (e: Exception) {
             val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(event.title)
                    .snippet(event.location)
            )
            marker?.tag = event
        }
    }

    private fun getCircularBitmapWithBorder(bitmap: Bitmap, borderWidth: Int = 4): Bitmap {
        val width = bitmap.width + borderWidth * 2
        val height = bitmap.height + borderWidth * 2

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint()
        paint.isAntiAlias = true

        val rect = Rect(0, 0, width, height)
        val rectF = RectF(rect)

        paint.color = Color.WHITE
        canvas.drawOval(rectF, paint)
        canvas.drawBitmap(bitmap, borderWidth.toFloat(), borderWidth.toFloat(), null)

        return output
    }


    private fun updateEventStatus(event: Event, newStatus: String) {
        val currentUser = auth.currentUser ?: return
        
        db.collection("users")
            .document(currentUser.uid)
            .collection("my_events")
            .document(event.id)
            .update("status", newStatus)
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToEventDetails(event: Event, imageView: ImageView?) {
        val bundle = bundleOf(
            "eventId" to event.id,
            "title" to event.title,
            "date" to event.date,
            "location" to event.location,
            "imageUrl" to event.imageUrl,
            "ticketUrl" to event.ticketUrl
        )

        if (imageView != null) {
            val transitionName = "event_image_${event.id}"
            val extras = FragmentNavigatorExtras(imageView to transitionName)
            findNavController().navigate(
                R.id.action_myInterests_to_eventDetails,
                bundle,
                null,
                extras
            )
        } else {
            findNavController().navigate(
                R.id.action_myInterests_to_eventDetails,
                bundle
            )
        }
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
        
        if (filteredList.isEmpty() && !isMapView) emptyTextView.visibility = View.VISIBLE

        Snackbar.make(recyclerView, "${event.title} removed", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                filteredList.add(position, event)
                eventList.add(event)
                adapter.notifyItemInserted(position)
                if (!isMapView) emptyTextView.visibility = View.GONE
                updateMapMarkers() 
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, eventId: Int) {
                    if (eventId != DISMISS_EVENT_ACTION) {
                        permanentlyDeleteEvent(event)
                    }
                }
            })
            .show()
            
        updateMapMarkers() 
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
        
        if (!isMapView) {
            emptyTextView.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
        }
        
        updateMapMarkers()
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
        mapView.onResume()
        searchView.clearFocus()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    // Helper method for MainActivity to call on tab reselection
    fun scrollToTop() {
        if (isMapView) {
            btnMapToggle.performClick()
        }
        recyclerView.smoothScrollToPosition(0)
    }
}
