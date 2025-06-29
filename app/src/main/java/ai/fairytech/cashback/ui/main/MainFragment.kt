package ai.fairytech.cashback.ui.main

import ai.fairytech.cashback.databinding.FragmentMainBinding
import ai.fairytech.cashback.ui.activity.CashbackActivity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        binding.buttonOpenWebView.setOnClickListener {
            val userId = binding.editTextInput.text.toString().trim()
            if (userId.isEmpty()) {
                binding.editTextInput.error = "User ID cannot be empty"
                return@setOnClickListener
            }
            context.getSharedPreferences("cashback_prefs", MODE_PRIVATE).edit()
                .putString("user_id", userId)
                .apply()
            val webViewIntent = Intent(context, CashbackActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webViewIntent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}