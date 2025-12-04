package com.example.budgettracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgettracker.databinding.FragmentSettingsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class settings : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadHistory()
    }

    private fun loadHistory() {
        val db = FirebaseFirestore.getInstance()

        db.collection("transactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->

                val sdf = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())

                val list = snap.documents.map {

                    val ts = it.getTimestamp("timestamp")?.toDate()
                    val formattedDate = if (ts != null) sdf.format(ts) else ""

                    TransactionModel(
                        amount = it.getLong("amount")?.toInt() ?: 0,
                        type = it.getString("type") ?: "",

                        //  أهم سطرين — ناخد priorityName لو موجود
                        priorityName = it.getString("priorityName")
                            ?: it.getString("itemTitle")
                            ?: "Unknown",

                        date = formattedDate
                    )
                }

                binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
                binding.rvHistory.adapter = TransactionHistoryAdapter(list)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
