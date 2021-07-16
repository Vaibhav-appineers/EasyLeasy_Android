package com.app.easyleasy.view.Subscription

import android.content.Context
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.easyleasy.R

import com.app.easyleasy.dataclasses.SubscriptionPlan

/*
*Offer Date using for Subscription Offers*/

class SubscriptionViewAdapter(/*val offerDate: Boolean,*/ val context: Context, var list: ArrayList<SubscriptionPlan>, private val subscriptionClickListener: SubscriptionClickListener) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedItemPos: Int = -1
    private var lastItemSelectedPos: Int = -1

    companion object {
        const val VIEW_TYPE_NORMAL_SUBS = 1
        const val VIEW_TYPE_DISCOUNTED_SUBS = 2
    }

    private inner class View1ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
        var tvPlanTitle: TextView = itemView.findViewById(R.id.tvPlanTitle)
        var tvPlanAmount: TextView = itemView.findViewById(R.id.tvPlanAmount)
        var ivMarkSelected: ImageView = itemView.findViewById(R.id.ivMarkSelected)
        var llSubscriptionContent: LinearLayout = itemView.findViewById(R.id.llSubscriptionContent)
        var tvDiscountPercent: TextView = itemView.findViewById(R.id.tvDiscountPercent)

        fun bind(position: Int) {
            if (position == selectedItemPos) {
                ivMarkSelected.visibility = View.VISIBLE
                llSubscriptionContent.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_un_selected)
            } else {
                ivMarkSelected.visibility = View.INVISIBLE
                llSubscriptionContent.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_un_selected)
            }

            val recyclerViewModel = list[position]
            tvPlanTitle.text = recyclerViewModel.planName
            tvPlanAmount.text = recyclerViewModel.planAmountForDisplay
            if (position == 1 && recyclerViewModel.planValidityInDays == "90") {
                tvDiscountPercent.visibility = View.VISIBLE
                tvDiscountPercent.text = "$3 Off"
            } else if (position == 2 && recyclerViewModel.planValidityInDays == "180") {
                tvDiscountPercent.visibility = View.VISIBLE
                tvDiscountPercent.text = "$12 Off"
            }  else if (position == 3 && recyclerViewModel.planValidityInDays == "365") {
                tvDiscountPercent.visibility = View.VISIBLE
                tvDiscountPercent.text = "$60 Off"
            } else {
                tvDiscountPercent.visibility = View.GONE

            }

            if (subscriptionClickListener != null) {
                itemView.setOnClickListener {
                    selectedItemPos = adapterPosition
                    if(lastItemSelectedPos == -1) {
                        list[position].isSelected = true
                        lastItemSelectedPos = selectedItemPos
                    } else {
                        list[lastItemSelectedPos].isSelected=false

                        lastItemSelectedPos = selectedItemPos
                    }
                    notifyDataSetChanged()
                    subscriptionClickListener.onSubscriptionClick(data = recyclerViewModel)
                }
            }
        }
    }

    private inner class View2ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {

        var tvPlanTitle: TextView = itemView.findViewById(R.id.tvPlanTitle)
        var tvPlanAmount: TextView = itemView.findViewById(R.id.tvPlanAmount)
        var ivMarkSelected: ImageView = itemView.findViewById(R.id.ivMarkSelected)
        var llSubsDiscountContent: LinearLayout = itemView.findViewById(R.id.llSubsDiscountContent)
        var tvDiscountPercent: TextView = itemView.findViewById(R.id.tvDiscountPercent)

        fun bind(position: Int) {
            if (position == selectedItemPos) {
                ivMarkSelected.visibility = View.VISIBLE
                llSubsDiscountContent.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_selected_new)
            } else {
                ivMarkSelected.visibility = View.INVISIBLE
                llSubsDiscountContent.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_un_selected)
            }

            val recyclerViewModel = list[position]
            tvPlanTitle.text = recyclerViewModel.planName

            if (position == 2 && recyclerViewModel.planValidityInDays == "90") {
                tvDiscountPercent.visibility = View.VISIBLE
                tvDiscountPercent.text = "$3 Off"
            } else if (position == 3 && recyclerViewModel.planValidityInDays == "180") {
                tvDiscountPercent.visibility = View.VISIBLE
                tvDiscountPercent.text = "$12 Off"
            }else if (position == 3 && recyclerViewModel.planValidityInDays == "365") {
                tvDiscountPercent.visibility = View.VISIBLE
                tvDiscountPercent.text = "$60 Off"
            } else {
                tvDiscountPercent.visibility = View.GONE

            }
            val payAmount = recyclerViewModel.planAmountForDisplay
            val spannableString1 = SpannableString(payAmount)
            if (payAmount != null) {
                spannableString1.setSpan(StrikethroughSpan(), 0, payAmount.length, 0)
            }
            tvPlanAmount.text = spannableString1
            if (subscriptionClickListener != null) {
                itemView.setOnClickListener {
                    selectedItemPos = adapterPosition
                    if(lastItemSelectedPos == -1) {
                        list[position].isSelected = true
                        lastItemSelectedPos = selectedItemPos
                    } else {
                        list[lastItemSelectedPos].isSelected=false

                        lastItemSelectedPos = selectedItemPos
                    }
                    notifyDataSetChanged()
                    subscriptionClickListener.onSubscriptionClick(data = recyclerViewModel)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_NORMAL_SUBS) {
            return View1ViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.subscription_plan_grid_item, parent, false)
            )
        }
        return View2ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.subscription_plan_discounted_grid_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 4 /*&& offerDate*/) {
            (holder as View2ViewHolder).bind(position)
        } else {
            (holder as View1ViewHolder).bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 4 /*&& offerDate*/) {
            VIEW_TYPE_DISCOUNTED_SUBS

        } else {
            VIEW_TYPE_NORMAL_SUBS
        }
    }
}