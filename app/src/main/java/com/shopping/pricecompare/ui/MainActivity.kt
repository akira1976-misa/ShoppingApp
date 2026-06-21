package com.shopping.pricecompare.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.shopping.pricecompare.R
import com.shopping.pricecompare.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 기본 setupWithNavController 대신 직접 처리
        // → 하단 탭은 항상 popUpTo(startDestination)으로 이동하게 함
        b.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.popBackStack(R.id.homeFragment, false)
                    if (navController.currentDestination?.id != R.id.homeFragment) {
                        navController.navigate(R.id.homeFragment)
                    }
                    true
                }
                R.id.categoryFragment -> {
                    navController.popBackStack(R.id.homeFragment, false)
                    navController.navigate(R.id.categoryFragment)
                    true
                }
                R.id.searchFragment -> {
                    navController.popBackStack(R.id.homeFragment, false)
                    navController.navigate(R.id.searchFragment)
                    true
                }
                else -> false
            }
        }

        // 화면 이동 시 하단 탭 선택 상태 자동 동기화
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment     -> b.bottomNav.menu.findItem(R.id.homeFragment)?.isChecked = true
                R.id.categoryFragment -> b.bottomNav.menu.findItem(R.id.categoryFragment)?.isChecked = true
                R.id.searchFragment,
                R.id.productListFragment -> b.bottomNav.menu.findItem(R.id.searchFragment)?.isChecked = true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
