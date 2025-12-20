package com.example.miniproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.miniproject.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✨ Animasi fade in untuk logo & text
        startAnimations()

        // Delay 3 detik lalu ke WelcomeActivity
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToWelcome()
        }, 3000)
    }

    private fun startAnimations() {
        // Fade in + scale animation untuk center container
        binding.centerContainer?.let { container ->
            container.alpha = 0f
            container.scaleX = 0.8f
            container.scaleY = 0.8f

            container.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setStartDelay(200)
                .start()
        }

        // Fade in untuk loading container
        binding.loadingContainer?.let { loading ->
            loading.alpha = 0f
            loading.translationY = 50f

            loading.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(500)
                .start()
        }

        // Fade in untuk version text
        binding.tvVersion?.let { version ->
            version.alpha = 0f
            version.animate()
                .alpha(0.5f)
                .setDuration(600)
                .setStartDelay(800)
                .start()
        }
    }

    private fun navigateToWelcome() {
        // Fade out animation sebelum pindah
        binding.root.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            .start()
    }
}