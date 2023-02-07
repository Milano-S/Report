package com.example.feedem.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.feedem.sharedPref.SharedPreference
import com.example.freedem.BuildConfig
import com.example.freedem.R
import com.example.freedem.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    //View Binding
    private lateinit var binding: FragmentAboutBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_about, container, false)
        setHasOptionsMenu(true)
        val pa = (activity as AppCompatActivity)
        val actionBar = pa.supportActionBar
        actionBar?.apply {
            title = ""
            hide()
        }
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAboutBinding.bind(view)
        binding.toolbarA.inflateMenu(R.menu.frag_menu)
        binding.toolbarA.setNavigationIcon(R.drawable.arrowback_2)
        binding.toolbarA.setNavigationOnClickListener { _ -> findNavController().popBackStack() }

        //App Version
        binding.tvAppVersion.text = "Version : "+ BuildConfig.VERSION_NAME

        //Device Id
        binding.tvDeviceId.text = "Device UUID : " + Build.ID

        //FCM Token
        //binding.tvFCM.text = "FCM Token : " + SharedPreference(requireContext()).getValueString("fcmToken")
        binding.tvFCM.text = "FCM Token : Unavailable"
    }
}