package com.example.ticketvalidation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

class WebViewFragment : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webView)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true

        arguments?.getString(ARG_URL)?.let {
            webView.loadUrl(it)
        }
    }

    companion object {
        private const val ARG_URL = "url"

        fun newInstance(url: String) = WebViewFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_URL, url)
            }
        }
    }
}
