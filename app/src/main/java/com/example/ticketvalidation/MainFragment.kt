package com.example.ticketvalidation

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class MainFragment : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvGreetings = view.findViewById<TextView>(R.id.tvGreetings)
        tvGreetings.apply {
            text = "Tickets Validation System"
            textSize = 6.dp.toFloat()
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(16, 16, 16, 16)
            setTextColor(Color.parseColor("#339966"))
        }
    }

    companion object {
        private const val ARG_URL = "url"

        fun newInstance(url: String) = MainFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_URL, url)
            }
        }
    }
}
