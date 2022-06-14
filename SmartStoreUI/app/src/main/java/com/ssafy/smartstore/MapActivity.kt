package com.ssafy.smartstore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ssafy.smartstore.databinding.ActivityMapBinding
import com.ssafy.smartstore.fragment.MapFragment

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val fragment = MapFragment()
        fragmentTransaction.add(R.id.map, fragment)
        fragmentTransaction.commit()

    }
}