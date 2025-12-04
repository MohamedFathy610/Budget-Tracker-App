package com.example.budgettracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class priorities : Fragment() {

    private lateinit var recycler: RecyclerView
    private val list = arrayListOf<PriorityModel>()
    private lateinit var adapter: PriorityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_priorities, container, false)

        // Hide FABs on this screen
        (requireActivity() as MainActivity).hideMainFab()

        recycler = view.findViewById(R.id.priorityRecycler)
        adapter = PriorityAdapter(list)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        val bottomPadding = resources.getDimensionPixelSize(R.dimen.fab_bottom_padding)
        recycler.setPadding(
            recycler.paddingLeft,
            recycler.paddingTop,
            recycler.paddingRight,
            bottomPadding
        )
        recycler.clipToPadding = false

        // Swipe to delete — حذف كل حاجة تخص الـ priority
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                override fun onMove(
                    rv: RecyclerView,
                    vh: RecyclerView.ViewHolder,
                    t: RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    val pos = viewHolder.adapterPosition
                    val item = list[pos]
                    val db = FirebaseFirestore.getInstance()

                    // 1) احذف كل المعاملات المرتبطة بالـ priority
                    db.collection("transactions")
                        .whereEqualTo("priorityId", item.id)
                        .get()
                        .addOnSuccessListener { snap ->

                            for (doc in snap.documents) {
                                doc.reference.delete()
                            }

                            // 2) حذف الـ priority نفسه
                            db.collection("priorities")
                                .document(item.id)
                                .delete()
                                .addOnSuccessListener {

                                    // 3) حذف من الليست
                                    list.removeAt(pos)
                                    adapter.notifyItemRemoved(pos)
                                }
                                .addOnFailureListener {
                                    adapter.notifyItemChanged(pos)
                                }
                        }
                        .addOnFailureListener {
                            adapter.notifyItemChanged(pos)
                        }
                }
            })

        itemTouchHelper.attachToRecyclerView(recycler)

        fetchData()

        return view
    }

    private fun fetchData() {
        FirebaseFirestore.getInstance()
            .collection("priorities")
            .get()
            .addOnSuccessListener { result ->

                list.clear()

                for (doc in result) {

                    val item = PriorityModel(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        amount = doc.getLong("amount")?.toInt(),
                        description = doc.getString("description") ?: ""
                    )

                    list.add(item)
                }

                adapter.notifyDataSetChanged()
            }
    }
}
