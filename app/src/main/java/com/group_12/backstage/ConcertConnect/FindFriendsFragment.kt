package com.group_12.backstage.ConcertConnect

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.group_12.backstage.R
import com.group_12.backstage.UserAccountData.User
import com.group_12.backstage.databinding.FragmentFindFriendsBinding

class FindFriendsFragment : Fragment() {

    private var _binding: FragmentFindFriendsBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private lateinit var requestsAdapter: FindFriendsAdapter
    private lateinit var searchAdapter: FindFriendsAdapter

    // Local cache of friend statuses: uid -> status ("sent", "received", "friend")
    private val friendStatuses = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupSearch()
        
        // Listen for changes in my friend list (requests, friends, etc.)
        listenToFriendStatuses()
    }

    private fun setupAdapters() {
        // Adapter for Requests List (Only shows received requests)
        requestsAdapter = FindFriendsAdapter(
            onAddClick = { /* Should not happen in requests list */ },
            onDeclineClick = { user -> declineRequest(user) },
            onAcceptClick = { user -> acceptRequest(user) }
        )
        binding.requestsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.requestsRecyclerView.adapter = requestsAdapter

        // Adapter for Search Results
        searchAdapter = FindFriendsAdapter(
            onAddClick = { user -> sendFriendRequest(user) },
            onDeclineClick = { /* Should not happen in search list */ },
            onAcceptClick = { user -> acceptRequest(user) }
        )
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.searchResultsRecyclerView.adapter = searchAdapter
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    searchUsers(query)
                } else {
                    // Clear search results if query is empty
                    searchAdapter.submitList(emptyList(), friendStatuses)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun listenToFriendStatuses() {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("users").document(myUid).collection("friends")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("FindFriends", "Listen failed.", e)
                    return@addSnapshotListener
                }

                friendStatuses.clear()
                val requestUids = mutableListOf<String>()

                for (doc in snapshots!!) {
                    val status = doc.getString("status") ?: continue
                    friendStatuses[doc.id] = status
                    if (status == "received") {
                        requestUids.add(doc.id)
                    }
                }

                // If we have requests, fetch their user profiles to display in the Requests section
                if (requestUids.isNotEmpty()) {
                    fetchUsersByIds(requestUids)
                } else {
                    binding.requestsContainer.isVisible = false
                    requestsAdapter.submitList(emptyList(), friendStatuses)
                }
                
                // Refresh search adapter to update buttons (e.g. if I just sent a request)
                searchAdapter.notifyDataSetChanged()
            }
    }

    private fun fetchUsersByIds(uids: List<String>) {
        if (uids.isEmpty()) return
        
        val topUids = uids.take(10)
        
        db.collection("users").whereIn("uid", topUids).get()
            .addOnSuccessListener { documents ->
                val users = documents.toObjects(User::class.java)
                binding.requestsContainer.isVisible = true
                requestsAdapter.submitList(users, friendStatuses)
            }
            .addOnFailureListener {
                Log.e("FindFriends", "Error fetching request users", it)
            }
    }

    private fun searchUsers(query: String) {
        binding.progressBar.isVisible = true
        
        db.collection("users")
            .orderBy("name")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(20)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.isVisible = false
                val myUid = auth.currentUser?.uid
                val users = documents.toObjects(User::class.java).filter { it.uid != myUid }
                
                searchAdapter.submitList(users, friendStatuses)
            }
            .addOnFailureListener {
                binding.progressBar.isVisible = false
                Log.e("FindFriends", "Search failed", it)
            }
    }

    private fun sendFriendRequest(user: User) {
        val myUid = auth.currentUser?.uid ?: return
        
        db.collection("users").document(myUid).collection("friends").document(user.uid)
            .set(mapOf("status" to "sent"))
            
        db.collection("users").document(user.uid).collection("friends").document(myUid)
            .set(mapOf("status" to "received"))
            .addOnSuccessListener {
                Toast.makeText(context, "Request sent to ${user.name}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun acceptRequest(user: User) {
        val myUid = auth.currentUser?.uid ?: return
        
        db.collection("users").document(myUid).collection("friends").document(user.uid)
            .update("status", "friend")
            
        db.collection("users").document(user.uid).collection("friends").document(myUid)
            .update("status", "friend")
            .addOnSuccessListener {
                Toast.makeText(context, "You are now friends with ${user.name}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun declineRequest(user: User) {
        val myUid = auth.currentUser?.uid ?: return
        
        db.collection("users").document(myUid).collection("friends").document(user.uid).delete()
        db.collection("users").document(user.uid).collection("friends").document(myUid).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Request declined", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
