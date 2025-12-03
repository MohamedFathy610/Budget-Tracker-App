package com.example.budgettracker

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.budgettracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    //هستخدمه ك switch
    private var clicked = false

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim)
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_close_anime)
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_anime)
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_anime)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ensure floating button are in front after layout is ready
        binding.root.post { bringFabsToFront() }

        // main floating button opens the floating menu
        binding.fabMain.setOnClickListener { toggleMenu() }

        // small fabs open fragments
        binding.fabPriorities.setOnClickListener {
            val frag = AddPriorityFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, frag)
                .addToBackStack(null)
                .commit()
            binding.root.post { bringFabsToFront() }
        }
//adding money floating button
        binding.fabAddMoney.setOnClickListener {
            val frag = MoneyOperationFragment().apply {
                arguments = Bundle().apply { putBoolean("isAdd", true) }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, frag)
                .addToBackStack(null)
                .commit()
            binding.root.post { bringFabsToFront() }
        }
//withdrawing money floating button
        binding.fabWithdraw.setOnClickListener {
            val frag = MoneyOperationFragment().apply {
                arguments = Bundle().apply { putBoolean("isAdd", false) }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, frag)
                .addToBackStack(null)
                .commit()
            binding.root.post { bringFabsToFront() }
        }

 // bottom navigation
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> openFragment(HomeFragment())
                R.id.priorities -> openFragment(priorities())
                R.id.settings -> openFragment(settings())
                else -> {}
            }
            true
        }

        // open home by default
        openFragment(HomeFragment())
    }
//applying the animation when clicking
    private fun toggleMenu() {
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }
//animation setting function
    private fun setAnimation(open: Boolean) {
        if (!open) {
            setVisibility(false)
            binding.fabMain.startAnimation(rotateOpen)
            binding.fabAddMoney.startAnimation(fromBottom)
            binding.fabWithdraw.startAnimation(fromBottom)
            binding.fabPriorities.startAnimation(fromBottom)
        } else {
            binding.fabMain.startAnimation(rotateClose)
            binding.fabAddMoney.startAnimation(toBottom)
            binding.fabWithdraw.startAnimation(toBottom)
            binding.fabPriorities.startAnimation(toBottom)

            toBottom.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    setVisibility(true)
                    setClickable(false)
                    toBottom.setAnimationListener(null)
                }
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
        }
    }
//so when you click two times it return it to invisible
    private fun setVisibility(isClosed: Boolean) {
        val visibility = if (isClosed) View.INVISIBLE else View.VISIBLE
        binding.fabAddMoney.visibility = visibility
        binding.fabWithdraw.visibility = visibility
        binding.fabPriorities.visibility = visibility
    }
//??? i forgot
    private fun setClickable(clicked: Boolean) {
        val clickable = !clicked
        binding.fabAddMoney.isClickable = clickable
        binding.fabWithdraw.isClickable = clickable
        binding.fabPriorities.isClickable = clickable
    }
//to be showing on the fragment
    private fun bringFabsToFront() {
        // bring to front in a stable order
        binding.fabMain.bringToFront()
        binding.fabAddMoney.bringToFront()
        binding.fabWithdraw.bringToFront()
        binding.fabPriorities.bringToFront()
    }
//opening fragment function
    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
        // ensure fab is visible above fragment
        binding.root.post { bringFabsToFront() }
    }
    //لانها private ف مش هعرف استخدمها في ال priorities fragment
    fun hideMainFab() {
        binding.fabMain.hide()
        binding.fabWithdraw.hide()
        binding.fabPriorities.hide()
        binding.fabAddMoney.hide()

    }
//عشان ارجع اظهرهم
    fun showMainFab() {
        binding.fabMain.show()
        binding.fabWithdraw.show()
        binding.fabPriorities.show()
        binding.fabAddMoney.show()
    }

}
