open class DataAdapterItem (val data: BookedTripShortData, val listener: ItemEventsListener) : IMyTripsAdapterItem, MyTripsItemTouchHelper.HelperListener {

    interface ItemEventsListener {
        fun onItemSelect(item: DataAdapterItem)
        fun onItemDelete(item: DataAdapterItem)
        fun onHelperViewOpen(item: DataAdapterItem)
        fun onHelperViewDrag(item: DataAdapterItem)
    }

    var selected: Boolean = false
        set(value) {field = value}

    private val touchHelper: MyTripsItemTouchHelper = MyTripsItemTouchHelper(this)

    override fun isSelected(): Boolean {
        return selected
    }

    override fun getType(): Int {
        return MY_TRIPS_ITEM_TYPE_DATA
    }

    override fun getId(): Int {
        return data.id
    }

    override fun isValidAddresses(): Boolean {
        return data.validAddresses
    }

    override fun bindViewHolder(context: Context?, holder: RecyclerView.ViewHolder, multiselect: Boolean) {
        if (holder is MyTripsViewHolder) {

            when(data.icon) {
                "taxi" -> holder.type.setImageResource(R.drawable.abc)
                "taxi_sh" -> holder.type.setImageResource(R.drawable.abc)
                "flex" -> holder.type.setImageResource(R.drawable.abc)
                "flex_sh" -> holder.type.setImageResource(R.drawable.abc)
            }

            val timeFormat = SimpleDateFormat(TIME_STRING_PATTERN, Locale.getDefault())

            holder.fromaddr.text = data.pickupAddress
            holder.toaddr.text = data.dropOffAddress
            holder.fromtime.text = timeFormat.format(data.pickupDate)

            val contentDescription = StringBuilder().append(data.travelTypeName)
            contentDescription.append(", ").append(context?.getString(R.string.abc, data.pickupAddress))
            contentDescription.append(", ").append(context?.getString(R.string.abc, data.dropOffAddress))
            contentDescription.append(", ").append(context?.getString(R.string.abc)).append(timeFormat.format(data.pickupDate))

            if(data.dropoffDate != null) {
                holder.totime.text = timeFormat.format( data.dropoffDate)
                holder.arrivalLabel.visibility = View.VISIBLE
                holder.arrowImage.visibility = View.VISIBLE
                contentDescription.append(", ").append(context?.getString(R.string.abc)).append(timeFormat.format(data.dropoffDate))
            } else {
                holder.totime.text = ""
                holder.arrivalLabel.visibility = View.INVISIBLE
                holder.arrowImage.visibility = View.INVISIBLE
            }

            if(data.isDeletable) {
                if (multiselect) {
                    if (selected) {
                        holder.front.show_details_button.setImageResource(R.drawable.abc)
                        holder.front.background = context?.getDrawable(R.drawable.abc)
                        holder.itemView.contentDescription = contentDescription.toString() + ", " + context?.getString(R.string.abc)
                    } else {
                        holder.front.show_details_button.setImageResource(R.drawable.abc)
                        holder.front.background = context?.getDrawable(android.R.color.white)
                        holder.itemView.contentDescription = contentDescription.toString() + ", " + context?.getString(R.string.abc)
                    }
                } else {
                    selected = false
                    holder.front.show_details_button.setImageResource(R.drawable.abc)
                    holder.front.background = context?.getDrawable(android.R.color.white)
                    holder.itemView.contentDescription = contentDescription.toString()
                }

                holder.delete.setOnClickListener {
                    listener.onItemDelete(this)
                }

                holder.itemView.setOnClickListener {
                    if (multiselect) {
                        selected = !selected
                        if (selected) {
                            holder.front.show_details_button.setImageResource(R.drawable.abc)
                            holder.front.background = context?.getDrawable(R.drawable.abc)
                            holder.itemView.contentDescription = contentDescription.toString() + ", " + context?.getString(R.string.abc)
                        } else {
                            holder.front.show_details_button.setImageResource(R.drawable.abc)
                            holder.front.background = context?.getDrawable(android.R.color.white)
                            holder.itemView.contentDescription = contentDescription.toString() + ", " + context?.getString(R.string.abc)
                        }
                    }
                    listener.onItemSelect(this)
                }

                if (!multiselect) {
                    touchHelper.bindToViewItem(context!!, holder.itemView)
                } else {
                    touchHelper.unbindFromViewItem()
                }
            } else { // not deletable
                selected = false

                holder.itemView.contentDescription = contentDescription.toString()

                holder.itemView.setOnClickListener {
                    listener.onItemSelect(this)
                }
            }
        }
    }

    override fun unbindViewHolder() {
        touchHelper.unbindFromViewItem()
    }

    override fun onOpen() {
        listener.onHelperViewOpen(this)
    }

    override fun onDrag() {
        listener.onHelperViewDrag(this)
    }

    fun closeHelperView(animated: Boolean) {
        touchHelper.close(animated)
    }
}

