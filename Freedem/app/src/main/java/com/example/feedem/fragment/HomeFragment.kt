package com.example.feedem.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.example.feedem.viewModel.ViewModelFeedem
import com.example.freedem.R
import com.example.freedem.databinding.FragmentHomeBinding
import com.example.feedem.room.inbox.ReportDatabaseBuilder
import com.example.feedem.room.saved.SavedReportBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val vm: ViewModelFeedem by activityViewModels()

    private lateinit var navCon: NavController

    private val dateFormatD = SimpleDateFormat("yyyy/MM/dd", Locale.US)

    private var dDay = ""
    private var dWeek = ""
    private var dMonth = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val parentActivity = (activity as AppCompatActivity)
        val actionBar = parentActivity.supportActionBar
        parentActivity.findViewById<DrawerLayout>(R.id.dlMain)
            .setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        actionBar?.apply {
            show()
            title = "Feedem"
            //setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(ColorDrawable(Color.parseColor("#262525")))
        }
        val w = parentActivity.window
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        w.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.feedemBlack)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        inboxCount()
        if (inboxCount() != "0") {
            binding.tvInboxCount.text = inboxCount()
            //binding.tvInboxCount.text = "456"
        } else {
            binding.tvInboxCount.isVisible = false
            //binding.tvInboxCount.text = "456"
        }

        //Nav Controller
        navCon = findNavController()

        val startSelectableDate = Calendar.getInstance()
        startSelectableDate[2020, Calendar.OCTOBER] = 1

        //Day
        binding.btnDay.setOnClickListener {
            checkIfOffline()
            if (vm.checkForInternet(requireContext())){
                binding.cvCalendar.isVisible = true
            }
            binding.cvCalendar.bringToFront()
            vm.setDayDateV("")
            binding.cdrvCalendar.setCurrentMonth(Calendar.getInstance())
            val endSelectableDate = Calendar.getInstance()
            binding.cdrvCalendar.setSelectableDateRange(
                startSelectableDate,
                endSelectableDate
            )

            binding.cdrvCalendar.setSelectedDateRange(
                Calendar.getInstance(),
                Calendar.getInstance()
            )

            binding.btnCloseCalendar.setOnClickListener {
                binding.cvCalendar.isVisible = false
            }

            val calendarListener: CalendarListener = object : CalendarListener {
                override fun onFirstDateSelected(startDate: Calendar) {

                    Log.d(TAG, startDate.toString())

                    val sDate = startDate
                    sDate[Calendar.DAY_OF_YEAR]

                    //startDate[Calendar.DAY_OF_YEAR] += 1
                    binding.cdrvCalendar.setSelectedDateRange(
                        sDate,
                        startDate
                    )
                    val dayFormat = dateFormatD.format(sDate.timeInMillis)
                    dDay = dayFormat
                    vm.setDayDateV(dayFormat)
                    binding.btnOkCalendar.setOnClickListener {
                        Toast.makeText(requireContext(), vm.dayDate, Toast.LENGTH_SHORT).show()
                        navCon.navigate(R.id.action_homeFragment_to_dayFragment)
                    }
                }

                override fun onDateRangeSelected(startDate: Calendar, endDate: Calendar) {
                    //Do Nothing
                }
            }
            if (vm.dayDate == "") {
                binding.btnOkCalendar.setOnClickListener {
                    vm.setDayDateV(dateFormatD.format(Calendar.getInstance().timeInMillis))
                    Toast.makeText(requireContext(), vm.dayDate, Toast.LENGTH_SHORT).show()
                    navCon.navigate(R.id.action_homeFragment_to_dayFragment)
                }
            }
            binding.cdrvCalendar.setCalendarListener(calendarListener)
        }

        //Week
        binding.btnWeek.setOnClickListener {
            checkIfOffline()
            if (vm.checkForInternet(requireContext())){
                binding.cvCalendar.isVisible = true
            }
            binding.cvCalendar.bringToFront()
            val dateFormatD = SimpleDateFormat("yyyy/MM/dd", Locale.US)
            vm.setWeekDateV(dateFormatD.format(Calendar.getInstance().timeInMillis))
            vm.setWeekDateV("")
            binding.cdrvCalendar.setCurrentMonth(Calendar.getInstance())
            val endSelectableDate = Calendar.getInstance()
            binding.cdrvCalendar.setSelectableDateRange(
                startSelectableDate,
                endSelectableDate
            )

            binding.cdrvCalendar.resetAllSelectedViews()
            val startSelectedDate = Calendar.getInstance()
            startSelectedDate[Calendar.DAY_OF_YEAR] =
                Calendar.getInstance()[Calendar.DAY_OF_YEAR] - Calendar.getInstance()[Calendar.DAY_OF_WEEK]
            binding.cdrvCalendar.setSelectedDateRange(
                startSelectedDate,
                Calendar.getInstance()
            )

            binding.btnCloseCalendar.setOnClickListener {
                binding.cvCalendar.isVisible = false
            }

            val calendarListener: CalendarListener = object : CalendarListener {
                override fun onFirstDateSelected(startDate: Calendar) {
                    val dayInMillis = 86400000

                    val sDate = Calendar.getInstance()
                    //sDate.timeInMillis = startDate.timeInMillis - weekInMillis
                    if (startDate[Calendar.DAY_OF_WEEK] == 7) {
                        sDate.timeInMillis = startDate.timeInMillis
                        startDate.timeInMillis = sDate.timeInMillis + (dayInMillis * 6)
                    } else {
                        sDate.timeInMillis =
                            startDate.timeInMillis - (startDate[Calendar.DAY_OF_WEEK] * dayInMillis)
                        startDate.timeInMillis = sDate.timeInMillis + (dayInMillis * 6)
                    }

                    try {
                        binding.cdrvCalendar.setSelectedDateRange(
                            sDate,
                            startDate
                        )
                    } catch (e: java.lang.Exception) {
                        binding.cdrvCalendar.setSelectedDateRange(
                            startSelectedDate,
                            Calendar.getInstance()
                        )
                    }
                    val weekFormat = dateFormatD.format(sDate.timeInMillis)
                    dWeek = weekFormat
                    binding.btnOkCalendar.setOnClickListener {
                        vm.setWeekDateV(weekFormat)
                        Toast.makeText(
                            requireContext(),
                            dateFormatD.format(sDate.timeInMillis) + " to " + dateFormatD.format(
                                startDate.timeInMillis
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                        navCon.navigate(R.id.action_homeFragment_to_weekFragment)
                    }
                }

                override fun onDateRangeSelected(startDate: Calendar, endDate: Calendar) {
                    //Do Nothing
                }
            }
            if (vm.weekDate == "") {
                binding.btnOkCalendar.setOnClickListener {
                    vm.setWeekDateV(dateFormatD.format(Calendar.getInstance().timeInMillis))
                    Toast.makeText(requireContext(), vm.weekDate, Toast.LENGTH_SHORT).show()
                    navCon.navigate(R.id.action_homeFragment_to_weekFragment)
                }
            }

            binding.cdrvCalendar.setCalendarListener(calendarListener)
        }

        //Month
        binding.btnMonth.setOnClickListener {

            checkIfOffline()
            if (binding.cvCalendar.isVisible) {
                binding.cvCalendar.isVisible = false
            }
            if (vm.checkForInternet(requireContext())){
                binding.cvCalendar.isVisible = true
            }
            binding.cdrvCalendar.setCurrentMonth(Calendar.getInstance())
            val monthF = SimpleDateFormat("MMMM", Locale.US)
            vm.setMonthDateV("")

            val endSelectableDate = Calendar.getInstance()

            binding.cdrvCalendar.resetAllSelectedViews()
            binding.cdrvCalendar.setSelectableDateRange(
                startSelectableDate,
                endSelectableDate
            )

            val startSelectedDate = Calendar.getInstance()
            startSelectedDate[Calendar.DAY_OF_YEAR] =
                Calendar.getInstance()[Calendar.DAY_OF_YEAR] - Calendar.getInstance()[Calendar.DAY_OF_MONTH] + 1

            binding.cdrvCalendar.setSelectedDateRange(
                startSelectedDate,
                Calendar.getInstance()
            )
            val dateFormatD = SimpleDateFormat("yyyy/MM/dd")
            val monthFormat = dateFormatD.format(Calendar.getInstance().timeInMillis)
            dMonth = monthFormat
            binding.btnCloseCalendar.setOnClickListener {
                binding.cvCalendar.isVisible = false
            }

            val calendarListener: CalendarListener = object : CalendarListener {
                override fun onFirstDateSelected(startDate: Calendar) {
                    val dayInMillis = 86400000L

                    val sDate = Calendar.getInstance()
                    val daysInMonth = getDaysInMonth(startDate).toLong()
                    //sDate.timeInMillis = startDate.timeInMillis - weekInMillis
                    sDate.timeInMillis =
                        startDate.timeInMillis - (startDate[Calendar.DAY_OF_MONTH] * dayInMillis)
                    startDate.timeInMillis = sDate.timeInMillis + (dayInMillis * daysInMonth)

                    sDate.timeInMillis += dayInMillis
                    //startDate.timeInMillis = dayInMillis

                    vm.setMonthDateV(monthFormat)
                    vm.setDateInMillis(startDate.timeInMillis)
                    try {
                        binding.cdrvCalendar.setSelectedDateRange(
                            sDate,
                            startDate
                        )
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }

                    binding.btnOkCalendar.setOnClickListener {
                        Toast.makeText(
                            requireContext(),
                            monthF.format(sDate.timeInMillis),
                            Toast.LENGTH_SHORT
                        ).show()
                        navCon.navigate(R.id.action_homeFragment_to_monthFragment)
                    }
                }

                override fun onDateRangeSelected(startDate: Calendar, endDate: Calendar) {
                    //Do Nothing
                }
            }
            if (vm.monthDate == "") {
                binding.btnOkCalendar.setOnClickListener {
                    vm.setMonthDateV(dateFormatD.format(Calendar.getInstance().timeInMillis))
                    vm.setDateInMillis(Calendar.getInstance().timeInMillis)
                    Toast.makeText(
                        requireContext(),
                        monthF.format(Calendar.getInstance().timeInMillis),
                        Toast.LENGTH_SHORT
                    ).show()
                    navCon.navigate(R.id.action_homeFragment_to_monthFragment)
                }
            }
            binding.cdrvCalendar.setCalendarListener(calendarListener)

        }

        //Inbox
        binding.btnInbox.setOnClickListener {
            if (binding.cvCalendar.isVisible) {
                binding.cvCalendar.isVisible = false
            }
            navCon.navigate(R.id.inboxFragment)
        }

        //Saved
        binding.btnSaved.setOnClickListener {
            navCon.navigate(R.id.action_homeFragment_to_savedFragment)
        }

        checkIfOffline()
    }

    private fun checkIfOffline() {
        if (runBlocking { !vm.checkForInternet(requireContext()) }) {
            //Day
            binding.btnDay.isClickable = false
            binding.btnDay.isEnabled = false
            //binding.btnDay.isVisible = false
            binding.tvDay.setTextColor(Color.GRAY)

            //Week
            binding.btnWeek.isClickable = false
            binding.btnWeek.isEnabled = false
            //binding.btnWeek.isVisible = false
            binding.tvWeek.setTextColor(Color.GRAY)

            //Month
            binding.btnMonth.isClickable = false
            binding.btnMonth.isEnabled = false
            //binding.btnMonth.isVisible = false
            binding.tvMonth.setTextColor(Color.GRAY)

            binding.btnSaved.isVisible = true

            Toast.makeText(requireContext(), "Offline", Toast.LENGTH_SHORT).show()
        }
    }

    private fun inboxCount(): String {
        val dbI = ReportDatabaseBuilder.getInstance(requireContext()).ReportDao()
        val inboxItems = runBlocking { dbI.getAllReports() }
        var inboxCount = 0
        inboxItems.forEach { item ->
            if (!item.isRead) {
                inboxCount++
            }
        }
        return inboxCount.toString()
    }

    private fun getDaysInMonth(date: Calendar): Int {
        return date.getActualMaximum(Calendar.DATE)
    }

    private fun getSavedReports() {
        val dbI = SavedReportBuilder.getInstance(requireContext()).SavedReportDao()
        val sL = runBlocking { dbI.getSavedReports() }
        vm.savedReportList.clear()
        vm.setSavedReportLisV(sL)
    }
}