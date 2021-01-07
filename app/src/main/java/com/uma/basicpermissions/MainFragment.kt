package com.uma.basicpermissions

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.google.android.material.snackbar.Snackbar
import com.uma.basicpermissions.databinding.FragmentMainBinding
import com.uma.basicpermissions.permissions.Permission
import com.uma.basicpermissions.permissions.PermissionManager


class MainFragment : Fragment() {

    private val binding by lazy { FragmentMainBinding.inflate(layoutInflater) }
    private val permissionManager = PermissionManager.from(this)


    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.camera.setOnClickListener {
            permissionManager.request(Permission.Camera)
                .rationale("Permission Check")
                .checkPermission {
                    if (it) {
                        success("Done")
                    } else {
                        error("failed")
                    }
                }
        }

        binding.bundled.setOnClickListener {
            permissionManager
                // Check a few bundled permissions under one: Storage = Read + Write
                .request(Permission.MandatoryForFeatureOne)
                .rationale("We require to demonstrate that we can request two permissions at once")
                .checkPermission { granted ->
                    if (granted) {
                        success("YES! Now I can access Storage and Location!")
                    } else {
                        error("Still missing at least one permission :(")
                    }
                }
        }

        binding.everything.setOnClickListener {
            permissionManager
                // Check all permissions without bundling them
                .request(Permission.Storage, Permission.Location, Permission.Camera)
                .rationale("We want permission for Storage (Read+Write), Location (Fine+Coarse) and Camera")
                .checkDetailedPermission { result ->
                    if (result.all { it.value }) {
                        success("YES! Now I have full access :D")
                    } else {
                        showErrorDialog(result)
                    }
                }
        }

        binding.clear.setOnClickListener {
            val manager =
                requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            manager.clearApplicationUserData()
        }
    }

    private fun showErrorDialog(result: Map<Permission, Boolean>) {
        val message = result.entries.fold("") { message, entry ->
            message + "${getErrorMessageFor(entry.key)}: ${entry.value}\n"
        }
        Log.i("TAG", message)
        AlertDialog.Builder(requireContext())
            .setTitle("Missing permissions")
            .setMessage(message)
            .show()
    }

    private fun getErrorMessageFor(permission: Permission) = when (permission) {
        Permission.Camera -> "Camera permission: "
        Permission.Location -> "Location permission"
        Permission.Storage -> "Storage permission"
        else -> "Unknown permission"
    }


    private fun success(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .withColor(Color.parseColor("#09AF00"))
            .show()
    }

    private fun error(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .withColor(Color.parseColor("#B00020"))
            .show()
    }

    private fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar {
        this.view.setBackgroundColor(colorInt)
        return this
    }


}