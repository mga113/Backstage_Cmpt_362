package com.group_12.backstage.Chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.group_12.backstage.R
import com.google.firebase.firestore.ListenerRegistration

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private val messages = mutableListOf<Message>()

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        recyclerView = view.findViewById(R.id.messagesRecyclerView)
        messageEditText = view.findViewById(R.id.messageEditText)
        sendButton = view.findViewById(R.id.sendButton)

        setupRecyclerView()
        setupSendButton()
        listenForMessages()

        return view
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(messages) { message ->
            // Optional: handle clicks
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            val text = messageEditText.text.toString()
            if (text.isNotBlank()) {
                sendMessage("Me", text) // Replace "Me" with actual username
                messageEditText.text.clear()
            }
        }
    }

    private fun sendMessage(sender: String, message: String) {
        val chatId = "chat_001" // same chat
        val msg = hashMapOf(
            "senderId" to sender,
            "text" to message
        )
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(msg)
    }


    private fun listenForMessages() {
        val chatId = "chat_001" // hardcoded for now
        listener = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                if (snapshots != null) {
                    messages.clear()
                    for (doc in snapshots.documents) {
                        val sender = doc.getString("senderId") ?: ""
                        val message = doc.getString("text") ?: ""
                        messages.add(Message(sender, message))
                    }
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
    }
}
