class MyTripsItemTouchHelper(val listener: HelperListener): View.OnTouchListener {

    interface HelperListener {
        fun onDrag()
        fun onOpen()
    }

    var frontView: View? = null
    var deleteButton: View? = null

    var touchSlop: Int = 0
    var maxOffsetX: Float = 0f

    var drag: Boolean = false
    var opened: Boolean = false
    var frontStartX: Float = 0f
    var motionStartX: Float = 0f
    var motionPrevX: Float = 0f
    var motionDiff: Float = 0f


    override fun onTouch(v: View?, e: MotionEvent?): Boolean {
        frontView?.let {fv ->
            e?.let {
                if(e.x < fv.x + fv.width) {
                    when (e.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            drag = false
                            motionStartX = e.x
                            motionPrevX = motionStartX
                            frontStartX = fv.x
                        }

                        MotionEvent.ACTION_MOVE -> {
                            if(kotlin.math.abs(e.x - motionStartX) > touchSlop) {
                                deleteButton?.visibility = View.VISIBLE
                                drag = true
                            }

                            if(drag) {
                                motionDiff = e.x - motionPrevX
                                motionPrevX = e.x

                                fv.x = kotlin.math.min(
                                    0f,
                                    kotlin.math.max(frontStartX + (e.x - motionStartX), maxOffsetX)
                                )
                                fv.invalidate()

                                listener.onDrag()
                            }
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            if(drag) {
                                if (motionDiff < 0 && fv.x < maxOffsetX / 4) {
                                    open()
                                } else {
                                    close()
                                }
                            }

                            if(!drag && e.actionMasked == MotionEvent.ACTION_UP) {
                                if(fv.x < 0f) {
                                    close()
                                } else {
                                    v?.performClick()
                                }
                            }

                            drag = false
                        }
                    }
                    return true
                }
            }
        }
        return false
    }

    fun bindToViewItem(context: Context, view: View) {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop

        frontView = view.my_trips_item_front_layer
        deleteButton = view.delete_trip_button

        view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            maxOffsetX = (deleteButton?.x ?: 0f) - view.width
        }

        if(opened) {
            open(false)
        } else {
            close(false)
        }

        view.setOnTouchListener(this)
    }

    fun unbindFromViewItem() {
        frontView = null
        deleteButton = null
    }

    fun open(animated: Boolean = true) {
        opened = true
        frontView?.let {fv ->
            if(animated) {
                fv.tag = ObjectAnimator.ofFloat(fv, "x", maxOffsetX).apply {
                    setAutoCancel(true)
                    duration = 100
                    start()
                }
            } else {
                if(fv.tag != null) {
                    (fv.tag as ObjectAnimator).cancel()
                }
                fv.x = maxOffsetX
                fv.invalidate()
            }
        }

        deleteButton?.visibility = View.VISIBLE
        deleteButton?.isClickable = true
        deleteButton?.isEnabled = true

        listener.onOpen()
    }

    fun close(animated: Boolean = true) {
        opened = false
        frontView?.let {fv ->
            if(animated) {
                fv.tag = ObjectAnimator.ofFloat(fv, "x", 0f).apply {
                    setAutoCancel(true)
                    duration = 100
                    start()
                    addListener(onEnd = {deleteButton?.visibility = View.INVISIBLE})
                }
            } else {
                if(fv.tag != null) {
                    (fv.tag as ObjectAnimator).cancel()
                }
                fv.x = 0f
                fv.invalidate()
            }
        }

        deleteButton?.isClickable = false
        deleteButton?.isEnabled = false
    }
}