package com.sitegist.ticketvalidation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import com.sitegist.ticketvalidation.data.Ticket

@Suppress("DEPRECATION")
class MainFragment : Fragment() {

    private var ticket: Ticket = Ticket(status = "", message = "", order_id = "")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            ticket = it.getParcelable("ticket" )!!
        }

        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()

        if (ticket.status != "") {
            InfoSheet().show(ctx) {
                style(SheetStyle.DIALOG)
                displayNegativeButton(false)
                cancelableOutside(false)
                drawable(
                    IconicsDrawable(
                        ctx,
                        GoogleMaterial.Icon.gmd_check
                    ).apply {
                        sizeDp = 32
                    }
                )
                drawableColor(R.color.md_theme_light_primary)
                title("Success!")
                content("${ticket.message}\n\nId: ${ticket.order_id}")
                onPositive {
                    // TODO: Display Ticket Additional Info
                }
            }
        } else {
            // TODO: Display Alert for Invalid Ticket
        }

    }

}
