package com.group_12.backstage

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.group_12.backstage.MyInterests.MyInterestsFragment
import com.group_12.backstage.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Switch back to the main app theme before super.onCreate
        setTheme(R.style.Theme_Backstage_group_12)
        
        super.onCreate(savedInstanceState)
        
        // Force White Status Bar with Dark Icons Programmatically
        window.statusBarColor = Color.WHITE
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use supportFragmentManager to get the NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setBackgroundColor(
            ContextCompat.getColor(this, R.color.dark_blue)
        )
        binding.bottomNavigationView.itemRippleColor =
            ContextCompat.getColorStateList(this, R.color.nav_ripple)

        // Handle Tab Reselection (Scroll to Top / Refresh)
        binding.bottomNavigationView.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.navigation_my_interests) {
                val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
                if (currentFragment is MyInterestsFragment) {
                    currentFragment.scrollToTop()
                }
            }
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("test").document("hello")
            .set(mapOf("msg" to "Hello Firestore"))
            .addOnSuccessListener { Log.d("Firestore", "Success") }
            .addOnFailureListener { e -> Log.e("Firestore", "Fail", e) }
    }
}
