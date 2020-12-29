package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.LocationUtils
import com.udacity.project4.utils.PermissionsResultEvent
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.toLatLng
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    override val baseViewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_select_location, container, false
        )

        binding.lifecycleOwner = this
        binding.onSaveButtonClicked = View.OnClickListener { onLocationSelected() }

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        setupGoogleMap()
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected

        return binding.root
    }

    private fun setupGoogleMap() {
        val mapFragment = childFragmentManager
            .findFragmentByTag(getString(R.string.map_fragment)) as? SupportMapFragment
            ?: return

        mapFragment.getMapAsync(this)
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        fun setMapType(mapType: Int): Boolean {
            map.mapType = mapType
            return true
        }

        return when (item.itemId) {
            R.id.normal_map -> setMapType(GoogleMap.MAP_TYPE_NORMAL)
            R.id.hybrid_map -> setMapType(GoogleMap.MAP_TYPE_HYBRID)
            R.id.terrain_map -> setMapType(GoogleMap.MAP_TYPE_TERRAIN)
            R.id.satellite_map -> setMapType(GoogleMap.MAP_TYPE_SATELLITE)

            else -> false
        }
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        startAtCurrentLocation()
    }

    private fun locationPermissionHandler(event: PermissionsResultEvent, handler: () -> Unit) {
        if (event.areAllGranted) {
            handler()
            return
        }

        if (event.shouldShowRequestRationale) {
            baseViewModel.showSnackBar.postValue(getString(R.string.permission_denied_explanation))
        }
    }

    private fun startAtCurrentLocation() {
        if (!LocationUtils.hasLocationPermissions()) {
            LocationUtils.requestPermissions {
                locationPermissionHandler(it, this::startAtCurrentLocation)
            }

            return
        }

        LocationUtils.requestSingleUpdate {
            val cameraPosition = CameraPosition.fromLatLngZoom(it.toLatLng(), 15f)
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)

            map.animateCamera(cameraUpdate)
        }
    }
}
