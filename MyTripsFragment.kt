class MyTripsFragment : Fragment(), MyTripsAdapter.MyTripsAdapterListener {

    lateinit var viewModel: MainViewModel
    lateinit var popupWindow: PopupWindow
    lateinit var adapter: MyTripsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_trips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val popupContent = layoutInflater.inflate(R.layout.my_trips_popup, null)
        popupWindow = PopupWindow(activity)
        popupWindow.contentView = popupContent
        popupWindow.setBackgroundDrawable(null)
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.elevation = Utils.dpToPixel(2f, context!!)
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true

        popupContent.my_trips_popup_item_cancel_multiple.setOnClickListener {
            popupWindow.dismiss()
            adapter.multiselect = true
            adapter.notifyDataSetChanged()
            updateView()
        }

        more_icon_image.setOnClickListener {
            if(!popupWindow.isShowing) {
                if(adapter.multiselect) {
                    MaterialAlertDialogBuilder(activity!!).setTitle(R.string.abc)
                        .setMessage(getString(R.string.abc, adapter.getSelectedCount()))
                        .setPositiveButton(getString(R.string.abc)) { dialog, _->
                            dialog.dismiss()
                            adapter.getSelectedIds()?. let { it ->
                                viewModel.run { deleteTrips(it, true, false) }
                            }

                            adapter.multiselect = false
                            adapter.notifyDataSetChanged()
                            updateView()
                        }
                        .setNegativeButton(getString(R.string.abc)) { dialog, _-> dialog.dismiss() }
                        .show()
                } else {
                    adapter.closeHelperViews()
                    popupWindow.showAsDropDown(it)
                }
            } else {
                popupWindow.dismiss()
            }
        }

        back_button_image.setOnClickListener {
            adapter.multiselect = false
            adapter.notifyDataSetChanged()
            updateView()
        }

        viewModel = ViewModelProvider(activity!!).get(MainViewModel::class.java)

        viewModel.getProgress().observe(viewLifecycleOwner, Observer{
            (activity as BaseActivity).showLoading(it, getString(R.string.abc))
        })

        viewModel.error.observe(viewLifecycleOwner, Observer {
            adapter.notifyDataSetChanged()

            val s = SpannableString(it) //  msg should have url to enable clicking
            Linkify.addLinks(s, Linkify.ALL)

            MaterialAlertDialogBuilder(activity!!).setPositiveButton(getString(R.string.abc)) { dialog, _-> dialog.dismiss()}
                .setMessage(Html.fromHtml(s.toString()))
                .show()

            updateView()
        })

        viewModel.setFromUntilMinutesForActiveTrip()

        viewModel.tripsList.observe(viewLifecycleOwner, Observer {
            if(viewModel.userHasPermits()) {
                adapter.setData(it)
                adapter.notifyDataSetChanged()

                updateView()
            }
//            if(!viewModel.directNewsAreShown) {
//                viewModel.directNewsAreShown = true
//                showDirectNews()
//            }
        })

        viewModel.isBooked().observe(viewLifecycleOwner, Observer {
            if(it) {
                viewModel.selectPermitById(viewModel.getBookedTrip().getTravelTypeId())
                // check if it is an active trip
                if(viewModel.isActiveTrip()) {
                   parentFragmentManager.beginTransaction().replace(
                        R.id.abc,
                        ActiveTripFragment()
                    ).addToBackStack("active_trip_fragment").commit()
                } else {
                    parentFragmentManager.beginTransaction().replace(
                        R.id.abc,
                        //ActiveTripFragment()
                        BookTripOverviewFragment()
                    ).addToBackStack("trip_overview_fragment").commit()
                }
            }
        })

        adapter = MyTripsAdapter(activity, this)
        my_trips_list.adapter = adapter
        my_trips_list.layoutManager = LinearLayoutManager(activity)

        if(viewModel.userHasPermits()) {
            if(!viewModel.directNewsAreShown) {
                viewModel.directNewsAreShown = true
                showDirectNews()
            } else {
                viewModel.getTripsList(true)
            }
        } else {
            adapter.setData(ArrayList())
            adapter.notifyDataSetChanged()
        }

        updateView()
    }

    override fun onItemSelected(position: Int) {
        if(adapter.multiselect) {
            updateView()
        } else {
            val id = adapter.getItemId(position)
            if(id != -1L) {

                //check for valid addresses
                if(adapter.isValidAddresses(position)) {
                    viewModel.getTripByOrderId(id.toInt())
                } else {
                    val s = SpannableString(viewModel.getNotValidAddressesText())
                    Linkify.addLinks(s, Linkify.ALL)
                    MaterialAlertDialogBuilder(context).setMessage(Html.fromHtml(s.toString()))
                        .setPositiveButton(getText(R.string.abc)) { dialog, _-> dialog.dismiss()}
                        .show()
                }
            }
        }
    }

    override fun onItemDelete(position: Int) {
        val id = adapter.getItemId(position)

        if(id != -1L) {
            viewModel.deleteTrips(listOf(id.toInt()), true, false)
        }
    }

    private fun updateView() {
        if(adapter.itemCount > 0) {
            no_trips_layout.visibility = View.GONE
            if(adapter.multiselect) {
                if(adapter.getSelectedCount() > 0) {
                    more_icon_image.setImageResource(R.drawable.abc)
                    more_icon_image.isEnabled = true
                    more_icon_image.isClickable = true
                } else {
                    more_icon_image.setImageResource(R.drawable.abc)
                    more_icon_image.isEnabled = false
                    more_icon_image.isClickable = false
                }
                more_icon_image.contentDescription = getString(R.string.abc)
                back_button_image.visibility = View.VISIBLE
                action_bar_title.setText(getString(R.string.abc, adapter.getSelectedCount() ))
            } else {
                more_icon_image.isEnabled = true
                more_icon_image.isClickable = true
                more_icon_image.setImageResource(R.drawable.abc)
                more_icon_image.contentDescription = getString(R.string.abc)
                back_button_image.visibility = View.GONE
                action_bar_title.setText(R.string.abc)
            }
        } else {
            more_icon_image.isEnabled = false
            more_icon_image.isClickable = false
            more_icon_image.setImageResource(R.drawable.abc)
            more_icon_image.contentDescription = getString(R.string.abc)
            back_button_image.visibility = View.GONE
            no_trips_layout.visibility = View.VISIBLE
        }
    }

    private fun showDirectNews() {
        viewModel.directedNews.observe(viewLifecycleOwner, Observer { news ->
            for(newsData in news) {
                showDirectedNewsDialog(newsData)
            }
        })
    }

    private fun showDirectedNewsDialog(news: NewsData) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.abc, null)
        val newsCheckBox = dialogView.findViewById<CheckBox>(R.id.abc)
        MaterialAlertDialogBuilder(context).setView(dialogView)
            .setPositiveButton(R.string.abc) { dialog, _ -> dialog.dismiss()
                if (newsCheckBox.isChecked) {
                    viewModel.setNewsAsRead(news.newsId)
                }
            }
            .show()

        dialogView.findViewById<TextView>(R.id.abc).text = news.newsHeader
        dialogView.findViewById<TextView>(R.id.abc).text = news.newsText
    }
}
