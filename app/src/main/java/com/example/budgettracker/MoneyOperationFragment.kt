package com.example.budgettracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.budgettracker.databinding.FragmentMoneyOperationBinding
import com.google.firebase.firestore.FirebaseFirestore
//عارفه انو موجود ك كلاس بس فيه مشكله في ال import بتاعه
data class PrioritySimple(
    val id: String = "",
    val title: String = "",
    val amount: Int = 0     // REMAINING amount needed
)

class MoneyOperationFragment : Fragment() {

    private var _binding: FragmentMoneyOperationBinding? = null
    private val binding get() = _binding!!

    private var isAdd = true
    private var priorityList: List<PrioritySimple> = emptyList()
// ؟؟؟؟؟؟؟؟
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAdd = arguments?.getBoolean("isAdd") ?: true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoneyOperationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleMoney.text = if (isAdd) "Add Money" else "Withdraw Money"

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        loadPriorities()

        binding.btnDone.setOnClickListener {
            doOperation()
        }
    }
//getting the data from the priorities
private fun loadPriorities() {
    FirebaseFirestore.getInstance()
        .collection("priorities")
        .get()
        .addOnSuccessListener { snap ->

            if (!isAdded) return@addOnSuccessListener   // حماية

            priorityList = snap.documents.map {
                PrioritySimple(
                    id = it.id,
                    title = it.getString("title") ?: "",
                    amount = it.getLong("amount")?.toInt() ?: 0
                )
            }

            val names = priorityList.map { it.title }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                names
            )

            binding.spinnerPriority.adapter = adapter
        }
}

    //الاهم هنا
private fun doOperation() {

    val enteredAmount = binding.inputAmount.text.toString().toIntOrNull()

    if (enteredAmount == null || enteredAmount <= 0) {
        binding.inputAmount.error = "Enter valid amount"
        return
    }

    if (priorityList.isEmpty()) {
        Toast.makeText(requireContext(), "No priorities available", Toast.LENGTH_SHORT).show()
        return
    }

    val selected = priorityList[binding.spinnerPriority.selectedItemPosition]

    val newRemaining = if (isAdd) {
        selected.amount - enteredAmount
    } else {
        selected.amount + enteredAmount
    }

    if (newRemaining < 0) {
        Toast.makeText(requireContext(), "Amount exceeds goal needed", Toast.LENGTH_SHORT).show()
        return
    }

    binding.btnDone.isEnabled = false

    val db = FirebaseFirestore.getInstance()

    db.collection("priorities").document(selected.id)
        .update("amount", newRemaining)
        .addOnSuccessListener {

            // -------------------------------
            // ADD TRANSACTION WITH TITLE FIXED
            // -------------------------------
            val data = hashMapOf(
                "priorityId" to selected.id,
                "itemTitle" to selected.title,   // ← هنا التعديل الحقيقي
                "amount" to enteredAmount,
                "type" to if (isAdd) "add" else "withdraw",
                "timestamp" to com.google.firebase.Timestamp.now()
            )


            db.collection("transactions")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Saved!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener {
                    binding.btnDone.isEnabled = true
                    Toast.makeText(requireContext(), "Failed to save transaction", Toast.LENGTH_SHORT).show()
                }
        }
        .addOnFailureListener {
            binding.btnDone.isEnabled = true
            Toast.makeText(requireContext(), "Failed to update priority", Toast.LENGTH_SHORT).show()
        }
}


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
