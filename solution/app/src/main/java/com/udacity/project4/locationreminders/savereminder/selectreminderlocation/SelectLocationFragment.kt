package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.LocationUtils
import com.udacity.project4.utils.PermissionsResultEvent
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.toLatLng
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    override val viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var selectedLocationMarker: Marker

    private lateinit var placeOfInterest: PointOfInterest

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

        return binding.root
    }

    private fun setupGoogleMap() {
        val mapFragment = childFragmentManager
            .findFragmentByTag(getString(R.string.map_fragment)) as? SupportMapFragment
            ?: return

        mapFragment.getMapAsync(this)
    }

    private fun onLocationSelected() {
        viewModel.setSelectedLocation(placeOfInterest)
        viewModel.navigationCommand.postValue(NavigationCommand.Back)
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

        val markerOptions = MarkerOptions()
            .position(map.cameraPosition.target)
            .title(getString(R.string.dropped_pin))
            .draggable(true)

        selectedLocationMarker = map.addMarker(markerOptions)

        viewModel.selectedPlaceOfInterest.value.let {
            placeOfInterest = it ?: PointOfInterest(map.cameraPosition.target, null, null)

            if (it == null) {
                startAtCurrentLocation()
            } else {
                selectedLocationMarker.position = it.latLng
                setCameraTo(it.latLng)
            }
        }

        map.setOnMapClickListener {
            selectedLocationMarker.position = it
            placeOfInterest = PointOfInterest(it, null, null)
        }

        map.setOnPoiClickListener {
            selectedLocationMarker.position = it.latLng
            placeOfInterest = it
        }
    }

    private fun locationPermissionHandler(event: PermissionsResultEvent, handler: () -> Unit) {
        if (event.areAllGranted) {
            handler()
            return
        }

        if (event.shouldShowRequestRationale) {
            viewModel.showSnackBar.postValue(getString(R.string.permission_denied_explanation))
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
            selectedLocationMarker.position = it.toLatLng()
            placeOfInterest = PointOfInterest(selectedLocationMarker.position, null, null)

            setCameraTo(selectedLocationMarker.position)
        }
    }

    private fun setCameraTo(latLng: LatLng) {
        val cameraPosition = CameraPosition.fromLatLngZoom(latLng, 15f)
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)

        map.animateCamera(cameraUpdate)
    }
}
