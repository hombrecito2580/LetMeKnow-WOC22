package com.example.letmeknow.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.example.letmeknow.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

//        bindProgressButton(binding.btnRegister)
//        binding.btnRegister.attachTextChangeAnimator()

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
//            lifecycleScope.launch {
//                binding.btnRegister.showProgress {
//                    buttonTextRes = R.string.loading
//                    progressColor = Color.WHITE
//                }
//                binding.btnRegister.isClickable = false
//                delay(2000)
//                binding.btnRegister.hideProgress("Success")
//            }
//            val intent = Intent(this, RegisterActivity::class.java)
//            val animationBundle = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right).toBundle()
//            startActivity(intent, animationBundle)
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}