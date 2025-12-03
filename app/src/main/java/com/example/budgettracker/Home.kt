package com.example.budgettracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.budgettracker.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        //to show the floating button only in the home
        (requireActivity() as MainActivity).showMainFab()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTotals()
    }

    private fun loadTotals() {
        //your firebase variable
        val db = FirebaseFirestore.getInstance()

        // total needed انا نسيت اغير اسم المتغيرات هنا  (sum of priorities amounts)
        db.collection("priorities")
            .get()
            .addOnSuccessListener { snap ->
                val totalSavings = snap.documents.sumOf { it.getLong("amount")?.toInt() ?: 0 }
                binding.valueSavings.text = "EGP $totalSavings"
            }

        // total input = sum(add) - sum(withdraw)
        db.collection("transactions")
            .get()
            .addOnSuccessListener { snap ->
                var total = 0
                for (doc in snap.documents) {
                    val amt = doc.getLong("amount")?.toInt() ?: 0
                    when (doc.getString("type")) {
                        "add" -> total += amt
                        "withdraw" -> total -= amt
                        else -> {}
                    }
                }
                binding.valueInput.text = "EGP $total"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
