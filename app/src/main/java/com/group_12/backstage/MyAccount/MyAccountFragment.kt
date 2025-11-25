package com.group_12.backstage.MyAccount

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.group_12.backstage.Authentication.LoginActivity
import com.group_12.backstage.databinding.FragmentMyAccountBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.net.Uri


class MyAccountFragment : Fragment(), MyAccountNavigator {

    private var _binding: FragmentMyAccountBinding? = null
    private val binding get() = _binding!!

    private val vm: MyAccountViewModel by viewModels()
    private lateinit var adapter: SettingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SettingsAdapter(this)

        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = adapter
        binding.list.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        binding.progress.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            vm.items.collectLatest { adapter.submitList(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh list to check for Auth changes (e.g. if coming back from LoginActivity)
        vm.refreshAuthStatus()
    }

    // --- Navigator callbacks ---
    override fun onSignInClicked() {
        // Launch LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onSignOutClicked() {
        FirebaseAuth.getInstance().signOut()
        Snackbar.make(binding.root, "Signed out successfully", Snackbar.LENGTH_SHORT).show()
        vm.refreshAuthStatus()
    }

    override fun onChevronClicked(id: String) {
        if (id == "sign_out") {
            onSignOutClicked()
        } else {
            Snackbar.make(binding.root, "Open: $id", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onSwitchChanged(id: String, enabled: Boolean) {
        // Update in-app state (and, later, Firestore via ViewModel)
        vm.updateToggle(id, enabled)

        // Handle OS-level behaviour
        when (id) {
            "receive_notifications" -> {
                // User wants notifications ON → check system permission
                if (enabled && !areNotificationsEnabled()) {
                    showNotificationsDisabledDialog()
                }
            }

            "location_content" -> {
                // User wants location-based content → check if location is enabled
                if (enabled && !isLocationEnabled()) {
                    showLocationDisabledDialog()
                }
            }
        }
    }


    override fun onEditClicked(id: String) {
        when (id) {
            "my_location" -> {
                showEditDialog(
                    id = id,
                    title = "Set your city & state",
                    hint = "City, State"
                )
            }
            "my_country" -> {
                showEditDialog(
                    id = id,
                    title = "Set your country",
                    hint = "Country"
                )
            }
            else -> {
                // Fallback – shouldn't really be hit for now
                Snackbar.make(binding.root, "Edit: $id", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditDialog(id: String, title: String, hint: String) {
        val current = vm.getCurrentValue(id) ?: ""

        val input = EditText(requireContext()).apply {
            setText(current)
            setSelection(current.length)
            this.hint = hint          // light hint text: "City, State" / "Country"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newValue = input.text.toString().trim()
                vm.updateValueRow(id, newValue)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---- Notifications helpers ----

    private fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
    }

    private fun showNotificationsDisabledDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Notifications disabled")
            .setMessage(
                "It appears as though notifications for this app are disabled. " +
                        "You can enable them in the app settings."
            )
            .setPositiveButton("Open Settings") { _, _ ->
                openNotificationSettings()
            }
            .setNegativeButton("OK", null)
            .show()
    }

    private fun openNotificationSettings() {
        val context = requireContext()
        val intent = Intent().apply {
            action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Settings.ACTION_APP_NOTIFICATION_SETTINGS
            } else {
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        startActivity(intent)
    }

    // ---- Location helpers ----

    private fun isLocationEnabled(): Boolean {
        val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showLocationDisabledDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location disabled")
            .setMessage(
                "It appears as though location services for this app are disabled. " +
                        "You can enable them in the system settings."
            )
            .setPositiveButton("Open Settings") { _, _ ->
                openLocationSettings()
            }
            .setNegativeButton("OK", null)
            .show()
    }

    private fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
