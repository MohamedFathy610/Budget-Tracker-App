package com.example.budgettracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.budgettracker.databinding.FragmentAddPriorityBinding
import com.google.firebase.firestore.FirebaseFirestore

class AddPriorityFragment : Fragment() {

    private var _binding: FragmentAddPriorityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddPriorityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancelButtonPriority.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
//done button to save the data of the new priority
        binding.doneButtonPriority.setOnClickListener {
            val title = binding.priorityTitle.text.toString().trim()
            val amount = binding.priorityAmount.text.toString().toIntOrNull()
            val description = binding.priorityDescription.text.toString().trim()
//عشان دايما ياخد title من اليوزر
            if (title.isEmpty()) {
                binding.priorityTitle.error = "Enter title"
                return@setOnClickListener
            }
//saving the information in the field
            binding.doneButtonPriority.isEnabled = false
            val priorityInfo = hashMapOf(
                "title" to title,
                "amount" to amount,
                "description" to description,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
//saving in the collection priority
            FirebaseFirestore.getInstance()
                .collection("priorities")
                .add(priorityInfo)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Saved!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed!", Toast.LENGTH_SHORT).show()
                    binding.doneButtonPriority.isEnabled = true
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
