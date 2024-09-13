package com.sitegist.ticketvalidation

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet
import com.sitegist.ticketvalidation.data.Ticket

class MainFragment : Fragment() {

    private var ticket: Ticket = Ticket(status = "", message = "", orderId = "")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            ticket = it.getParcelable("ticket")!!
        }

        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ticket.status != "") {
            InfoSheet().show(requireContext()) {
                style(SheetStyle.DIALOG)
                displayNegativeButton(false)
                cancelableOutside(false)
                title("Success!")
                drawableColor(R.color.md_theme_light_primary)
                content("${ticket.message} ${ticket.orderId}")
                onPositive { }
            }
        }

//        val appPrefs = Utils.getAppPreferences(requireContext())
//        val tvGreetings = view.findViewById<TextView>(R.id.tvGreetings)
//        tvGreetings.apply {
//            text = "User: ${appPrefs.user}"
//            textSize = 6.dp.toFloat()
//            textAlignment = View.TEXT_ALIGNMENT_CENTER
//            setPadding(16, 16, 16, 16)
//            setTextColor(Color.parseColor("#339966"))
//        }
    }

}
