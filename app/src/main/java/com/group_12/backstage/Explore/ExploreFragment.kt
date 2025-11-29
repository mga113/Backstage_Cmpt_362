package com.group_12.backstage.Explore

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.group_12.backstage.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ExploreFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventsAdapter
    private lateinit var tvNoResults: TextView
    private val events = mutableListOf<Event>()
    private lateinit var btnFilterDate: Button
    private lateinit var genreChipGroup: ChipGroup

    private var startDate: String? = null
    private var endDate: String? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var searchJob: Runnable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        recyclerView = view.findViewById(R.id.recyclerEvents)
        tvNoResults = view.findViewById(R.id.tvNoResults)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = EventsAdapter(events,
            onInterestedClick = { event -> markEventStatus(event, "interested") },
            onGoingClick = { event -> markEventStatus(event, "going") }
        )
        recyclerView.adapter = adapter

        val searchView = view.findViewById<SearchView>(R.id.searchBar)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                fetchEvents(query, getSelectedGenre(), startDate, endDate)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText ?: ""
                searchJob?.let { handler.removeCallbacks(it) }

                if (query.isEmpty()) {
                    // When clearing search, refresh but KEEP filters
                    events.clear()
                    adapter.updateEvents(events.toMutableList())
                    fetchEvents(query = "", genre = getSelectedGenre(), startDate = startDate, endDate = endDate)
                }

                searchJob = Runnable {
                    fetchEvents(
                        query = query,
                        genre = getSelectedGenre(),
                        startDate = startDate,
                        endDate = endDate
                    )
                }
                handler.postDelayed(searchJob!!, 350)
                return true
            }
        })

        genreChipGroup = view.findViewById<ChipGroup>(R.id.genreChipGroup)
        genreChipGroup.setOnCheckedChangeListener { _, checkedId ->
            val currentQuery = searchView.query.toString()

            if (checkedId == R.id.chipAll) {
                // Reset ALL filters including Date
                startDate = null
                endDate = null
                btnFilterDate.text = "Filter by Date" // Reset button text
                fetchEvents(currentQuery, null, null, null)
                Toast.makeText(context, "Showing all events", Toast.LENGTH_SHORT).show()
            } else {
                val genreFilter = when (checkedId) {
                    R.id.chipPop -> "Pop"
                    R.id.chipRock -> "Rock"
                    R.id.chipHipHop -> "Hip-Hop"
                    R.id.chipCountry -> "Country"
                    R.id.chipJazz -> "Jazz"
                    R.id.chipElectronic -> "Electronic"
                    else -> null
                }
                // Pass current date filters so they aren't lost
                fetchEvents(currentQuery, genreFilter, startDate, endDate)
            }
        }

        // Handle click on "All" chip specifically for when it is ALREADY selected
        val chipAll = view.findViewById<Chip>(R.id.chipAll)
        chipAll.setOnClickListener {
            // If date filters are active, clear them
            if (startDate != null || endDate != null) {
                startDate = null
                endDate = null
                btnFilterDate.text = "Filter by Date"
                val currentQuery = searchView.query.toString()
                fetchEvents(currentQuery, null, null, null)
                Toast.makeText(context, "Date filter cleared", Toast.LENGTH_SHORT).show()
            }
        }

        btnFilterDate = view.findViewById(R.id.btnFilterDate)
        btnFilterDate.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .setTheme(R.style.Theme_Backstage_DatePicker)
                .build()

            dateRangePicker.show(parentFragmentManager, "datePicker")
            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                val startMillis = selection.first
                val endMillis = selection.second ?: selection.first
                
                // Use UTC timezone to match MaterialDatePicker output
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                
                startDate = dateFormat.format(Date(startMillis))
                endDate = dateFormat.format(Date(endMillis))
                
                // Update Button Text
                if (startDate == endDate) {
                    btnFilterDate.text = startDate
                } else {
                    btnFilterDate.text = "$startDate to $endDate"
                }

                val currentQuery = searchView.query.toString()
                fetchEvents(currentQuery, getSelectedGenre(), startDate, endDate)
            }
        }
        fetchEvents() // loads concerts by default when Explore opens

        return view
    }

    private fun getSelectedGenre(): String? {
        val checkedChipId = genreChipGroup.checkedChipId
        // If "All" is checked, return null for genre
        if (checkedChipId == R.id.chipAll) return null
        
        return if (checkedChipId != View.NO_ID) {
            val selectedChip = genreChipGroup.findViewById<Chip>(checkedChipId)
            selectedChip.text.toString()
        } else {
            null
        }
    }

    private fun fetchEvents(
        query: String = "",
        genre: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        val apiKey = "A9nGiKzxYRZTGQfiUG0lK0JlNkZJ8FXx"
        val baseUrl = "https://app.ticketmaster.com/discovery/v2/events.json"

        var url = "$baseUrl?apikey=$apiKey&countryCode=CA&segmentName=Music"

        if (query.isNotEmpty()) {
            url += "&keyword=${Uri.encode(query)}"
        } else {
            url += "&size=50"
        }

        if (genre != null) {
            url += "&classificationName=${Uri.encode(genre)}"
        }

        if (startDate != null) {
            // Logic to handle Timezones correctly using standard startDateTime/endDateTime
            // We will extend the end date by 1 day in UTC to ensure we catch evening concerts
            // that occur in Pacific/Eastern time (which is next day UTC)
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                parser.timeZone = TimeZone.getTimeZone("UTC")
                
                val endStr = endDate ?: startDate
                val parsedEndDate = parser.parse(endStr)
                
                // Add 1 day to end date to create a safe buffer
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.time = parsedEndDate!!
                calendar.add(Calendar.DAY_OF_MONTH, 1) 
                
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                val safeEndDate = formatter.format(calendar.time)

                // UTC Format: yyyy-MM-ddTHH:mm:ssZ
                val startDateTime = "${startDate}T00:00:00Z"
                val endDateTime = "${safeEndDate}T23:59:59Z" // End of the NEXT day to be safe
                
                url += "&startDateTime=$startDateTime&endDateTime=$endDateTime"
                
            } catch (e: Exception) {
                Log.e("ExploreFragment", "Date format error", e)
            }
        }
        
        Log.d("ExploreFragment", "Fetching: $url")

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    events.clear()
                    val embedded = response.optJSONObject("_embedded")
                    
                    if (embedded == null) {
                        adapter.updateEvents(events.toMutableList())
                        tvNoResults.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        return@JsonObjectRequest
                    }
                    
                    val eventsArray = embedded.optJSONArray("events")
                    
                    if (eventsArray == null || eventsArray.length() == 0) {
                         adapter.updateEvents(events.toMutableList())
                        tvNoResults.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        return@JsonObjectRequest
                    }
                    
                    tvNoResults.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    for (i in 0 until eventsArray.length()) {
                        val e = eventsArray.getJSONObject(i)
                        val id = e.optString("id")
                        val name = e.optString("name", "Untitled")

                        val images = e.optJSONArray("images")
                        val imageUrl = images?.getJSONObject(0)?.optString("url", "") ?: ""

                        val venueObj = e.optJSONObject("_embedded")
                            ?.optJSONArray("venues")
                            ?.getJSONObject(0)

                        var venueName = venueObj?.optString("name", "Unknown Venue") ?: "Unknown Venue"
                        val city = venueObj?.optJSONObject("city")?.optString("name")
                        if (!city.isNullOrEmpty()) {
                            venueName += ", $city"
                        }

                        var lat = 0.0
                        var lng = 0.0
                        try {
                            val location = venueObj?.optJSONObject("location")
                            if (location != null) {
                                lat = location.optString("latitude", "0.0").toDouble()
                                lng = location.optString("longitude", "0.0").toDouble()
                            }
                        } catch (e: Exception) {
                            Log.e("Explore", "Error parsing location", e)
                        }

                        var formattedDate = ""
                        e.optJSONObject("dates")?.optJSONObject("start")?.let { startObj ->
                            val localDate = startObj.optString("localDate")
                            if (localDate.isNotEmpty()) {
                                try {
                                    val inputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    val outputFmt = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                                    formattedDate = outputFmt.format(inputFmt.parse(localDate)!!)
                                } catch (ex: Exception) {
                                    formattedDate = localDate
                                }
                            }
                        }

                        var genreName = ""
                        val classifications = e.optJSONArray("classifications")
                        if (classifications != null && classifications.length() > 0) {
                            genreName = classifications.getJSONObject(0)
                                .optJSONObject("genre")
                                ?.optString("name", "") ?: ""
                        }

                        events.add(Event(id, name, formattedDate, venueName, imageUrl, genreName, lat, lng))
                    }
                    adapter.updateEvents(events.toMutableList())
                } catch (ex: Exception) {
                    Log.e("API_PARSE", "Error parsing events: ${ex.message}")
                }
            },
            { error ->
                Log.e("API_ERROR", "Error fetching concerts: $error")
                Toast.makeText(context, "Error loading events", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun markEventStatus(event: Event, status: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Please log in to save events", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = user.uid
        val firestore = FirebaseFirestore.getInstance()

        val eventData = mapOf(
            "id" to event.id,
            "title" to event.name,
            "date" to event.date,
            "location" to event.venue,
            "imageUrl" to event.imageUrl,
            "genre" to event.genre,
            "status" to status,
            "ticketUrl" to "",
            "latitude" to event.latitude,
            "longitude" to event.longitude
        )

        firestore.collection("users")
            .document(uid)
            .collection("my_events")
            .document(event.id)
            .set(eventData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Marked as $status", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Firebase", "Error writing event", e)
            }
    }
}
