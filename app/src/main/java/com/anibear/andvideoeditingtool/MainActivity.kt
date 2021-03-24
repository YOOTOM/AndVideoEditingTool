package com.anibear.andvideoeditingtool

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.FragmentTransaction
import com.anibear.andvideoeditingtool.databinding.ActivityMainBinding
import com.anibear.andvideoeditingtool.fragments.MasterProcessorFragment

class MainActivity : AppCompatActivity() {
    private var fragmentTransaction: FragmentTransaction? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction?.replace(R.id.frame_container, MasterProcessorFragment())?.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fragmentTransaction != null)
            fragmentTransaction?.remove(MasterProcessorFragment())

        moveTaskToBack(true)
        finishAndRemoveTask()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}