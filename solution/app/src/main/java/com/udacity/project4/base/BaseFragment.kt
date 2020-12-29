package com.udacity.project4.base

import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.utils.PermissionManager

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment : PermissionManager() {
    /**
     * Every fragment has to have an instance of a view model that extends from the BaseViewModel
     */
    abstract val baseViewModel: BaseViewModel

    override fun onStart() {
        super.onStart()

        baseViewModel.showErrorMessage.observe(this) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }

        baseViewModel.showToast.observe(this) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }

        baseViewModel.showSnackBar.observe(this) {
            Snackbar.make(this.requireView(), it, Snackbar.LENGTH_LONG).show()
        }

        baseViewModel.showSnackBarInt.observe(this) {
            Snackbar.make(this.requireView(), getString(it), Snackbar.LENGTH_LONG).show()
        }

        baseViewModel.navigationCommand.observe(this) { command ->
            when (command) {
                is NavigationCommand.To -> findNavController().navigate(command.directions)
                is NavigationCommand.Back -> findNavController().popBackStack()
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )
            }
        }
    }
}