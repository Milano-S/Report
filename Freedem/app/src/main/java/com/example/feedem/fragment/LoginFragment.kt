package com.example.feedem.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.feedem.adapter.BaseUrlAdapter
import com.example.feedem.api.AuthInterface
import com.example.feedem.api.InboxInterface
import com.example.feedem.api.LoginInterface
import com.example.feedem.api.LoginResponse
import com.example.feedem.model.*
import com.example.feedem.room.inbox.ReportDatabaseBuilder
import com.example.feedem.room.url.UrlsDatabaseBuilder
import com.example.feedem.sharedPref.SharedPreference
import com.example.feedem.url.Urls
import com.example.feedem.viewModel.ViewModelFeedem
import com.example.freedem.R
import com.example.freedem.databinding.FragmentLoginBinding
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "LoginFragment"

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var fragContext: Context
    private lateinit var adapter: BaseUrlAdapter
    private lateinit var urlList: List<UrlsData>
    private val vm: ViewModelFeedem by activityViewModels()

    //Inbox Response
    private lateinit var inboxResponse: List<ReportData>

    //App Token Key
    private var appTokenKey = ""

    private val sp: SharedPreference by lazy { SharedPreference(requireContext()) }

    //User Token Key
    private var userTokenKey = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (vm.checkForInternet(requireContext())) {
            authenticateApp()
            if (sp.getValueBoolean("signedIn", false)) {
                userLogin(
                    LoginDetails(
                        sp.getValueString("userName").toString(),
                        sp.getValueString("userPassword").toString(),
                        "Android",
                        Build.VERSION.INCREMENTAL,
                        Build.BRAND,
                        Build.MODEL
                    ), sp.getValueString("appToken").toString()
                )
            }
        } else if (sp.getValueBoolean("signedIn", false)) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }

        urlList = runBlocking {
            UrlsDatabaseBuilder.getInstance(requireContext()).UrlDao().getAllBaseUrls()
        }
        //Authenticate App
        authenticateApp()
        val parentActivity = (activity as AppCompatActivity)
        val w = parentActivity.window
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        w.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.feedemOrange)

        val pa = (activity as AppCompatActivity)
        val actionBar = pa.supportActionBar
        actionBar?.apply {
            title = ""
            hide()
        }
        pa.findViewById<DrawerLayout>(R.id.dlMain).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        fragContext = requireContext()

        //binding.btnLogin.setOnClickListener {findNavController().navigate(R.id.homeFragment)}
        binding.btnLogin.setOnClickListener {
            binding.btnLogin.isEnabled = false
            binding.btnLogin.isClickable = false

            if (vm.checkForInternet(requireContext())) {
                if (binding.etUsername.text.toString().isEmpty() || binding.etPassword.text.toString().isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.login_empty),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.isClickable = true
                } else {
                    userLogin(
                        LoginDetails(
                            binding.etUsername.text.toString(),
                            binding.etPassword.text.toString(),
                            "Android",
                            Build.VERSION.INCREMENTAL,
                            Build.BRAND,
                            Build.MODEL
                        ), sp.getValueString("appToken").toString()
                    )
                }
            } else {
                Toast.makeText(requireContext(), "Not Signed In", Toast.LENGTH_SHORT).show()
                binding.btnLogin.isEnabled = true
                binding.btnLogin.isClickable = true
            }
        }

        binding.tvForgotPassword.setOnClickListener { Toast.makeText(requireContext(), "Forgot Password", Toast.LENGTH_SHORT).show() }

        //DEBUG MENU STUFF
        debugMenu()
    }

    private fun authenticateApp() {
        //val baseUrl = vm.baseUrl

        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Urls(requireContext()).baseUrl)
            .build()
            .create(AuthInterface::class.java)

        val authObject = AuthRequest(
            //ApplicationKey = Urls.appKey,
            ApplicationKey = "F0FCDA7B-1FA2-4FDB-AE2B-33D502FFB72F",
            AppVersion = "uat",
            OSName = "Android"
        )

        val retroFitData = retrofitBuilder.authorizeApp(authObject)

        Handler(Looper.getMainLooper()).postDelayed(
            {
                retroFitData.enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(
                        call: Call<AuthResponse>,
                        response: Response<AuthResponse>
                    ) {
                        if (response.body() != null) {
                            val appTokenKey = response.body()?.TokenKey
                            SharedPreference(fragContext).save("appToken", appTokenKey.toString())
                            binding.clSplash.isVisible = false
                            Log.i(TAG, appTokenKey.toString())
                        }else{
                            binding.clSplash.isVisible = false
                            Toast.makeText(
                                requireContext(),
                                "Authentication Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        binding.clSplash.isVisible = false
                        Toast.makeText(
                            requireContext(),
                            "Authentication Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.i(TAG, t.message.toString())
                    }
                })

            }, 800
        )
    }

    //Login
    private fun userLogin(loginDetails: LoginDetails, appTokenKey: String) {
        runBlocking { authenticateApp() }
        val response = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Urls(requireContext()).baseUrl)
            .build()
            .create(LoginInterface::class.java)

        response.userLogin(
            loginDetails,
            appTokenKey
        ).enqueue(
            object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.body()?.Success == true) {
                        userTokenKey = response.body()!!.TokenKey
                        vm.setUserTokenKeyV(userTokenKey)
                        getReportInbox(
                            FromDate("2022-09-14 00:00:00"),
                            vm.appTokenKey,
                            vm.userTokenKey
                        )
                        SharedPreference(requireContext()).save("signedIn", true)
                        SharedPreference(requireContext()).save("userName", loginDetails.UserName)
                        SharedPreference(requireContext()).save("userPassword", loginDetails.Password)
                        SharedPreference(requireContext()).save("userToken", userTokenKey)
                      /*  MyFirebaseMessagingService().sendRegistrationToServer(
                            PushNotificationRequest(
                                DeviceTypeId = 1,
                                DeviceUID = Settings.Secure.ANDROID_ID,
                                DeviceName = Build.BRAND,
                                DeviceDescription = Build.MODEL,
                                Token = SharedPreference(requireContext()).getValueString("fcmToken")
                                    .toString()
                            ), vm.userTokenKey, requireContext()
                        )*/
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    } else {
                        binding.etUsername.text?.clear()
                        binding.etPassword.text?.clear()
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.isClickable = true
                        if (sp.getValueString("userName") != null) {
                            Toast.makeText(
                                requireContext(),
                                R.string.login_error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    Log.i(TAG, response.body().toString())
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.isClickable = true
                    Log.i(TAG, t.message.toString())
                    Toast.makeText(requireContext(), R.string.login_error, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    //Inbox
    private fun getReportInbox(FromDate: FromDate, appTokenKey: String, userTokenKey: String) {

        val db = ReportDatabaseBuilder
        val dbI = db.getInstance(requireContext()).ReportDao()
        val sp = SharedPreference(requireContext())

        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Urls(requireContext()).baseUrl)
            .build()
            .create(InboxInterface::class.java)

        val retrofitData = retrofitBuilder.getInbox(
            FromDate,
            appTokenKey,
            userTokenKey
        )

        retrofitData.enqueue(
            object : Callback<DocumentResponse> {
                override fun onResponse(
                    call: Call<DocumentResponse>,
                    response: Response<DocumentResponse>
                ) {
                    inboxResponse = response.body()!!.Reports
                    vm.setInboxDocuments(inboxResponse)
                    CoroutineScope(Dispatchers.IO).launch {
                        dbI.insertAllReports(inboxResponse)
                    }
                    //Downloads All inbox items
                    if (sp.getValueString("inboxDownloadC") == null) {
                        inboxDownloadDialog()
                    } else if (sp.getValueString("inboxDownloadC") == "No") {
                        //Do Nothing
                        Log.i(TAG, sp.getValueString("inboxDownloadC").toString())
                    } else if (sp.getValueString("inboxDownloadC") == "Yes" && vm.isNotUsingWifi(
                            requireContext()
                        ) && sp.getValueBoolean("downloadWifi", true)
                    ) {
                        showToast("Download Over Wif-Fi Enabled")
                    } else {
                        val context = (activity as AppCompatActivity).applicationContext
                        showToast("Downloading Inbox Reports...")
                        CoroutineScope(Dispatchers.Default).launch {
                            var i = 0
                            inboxResponse.forEach { inbox ->
                                i++
                                Log.i(TAG, i.toString())
                                if (i == inboxResponse.size) {
                                    showToast("Inbox Download Complete")
                                }
                                vm.downloadPdf(
                                    context = context,
                                    url = inbox.DocumentUrl,
                                    title = inbox.DateTitle,
                                    documentKey = inbox.DocumentKey
                                )
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<DocumentResponse>, t: Throwable) {
                    Log.e(TAG, t.message.toString())
                }
            }
        )
    }

    //Inbox Dialog
    private fun inboxDownloadDialog() {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setCancelable(false)
            setTitle("Download Inbox Items?")
            setMessage("Downloads all inbox reports for offline viewing")
        }
        builder.setPositiveButton("Yes") { dialog, _ ->
            if (SharedPreference(requireContext()).getValueBoolean(
                    "downloadWifi",
                    true
                ) && vm.isNotUsingWifi(requireContext())
            ) {
                sp.save("inboxDownloadC", "Yes")
                val builder2 = AlertDialog.Builder(context)
                builder2.apply {
                    setCancelable(false)
                    setTitle("Not Connected to Wi-Fi")
                    setMessage(getString(R.string.no_wifi))
                    setPositiveButton("Ok") { dialog2, _ ->
                        dialog2.dismiss()
                        SharedPreference(requireContext()).save("inboxDownload", true)
                    }
                    show()
                }
            } else {
                sp.save("inboxDownloadC", "Yes")
                var i = 0
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch {
                    showToast("Downloading Inbox Reports...")
                    inboxResponse.forEach { inbox ->
                        i++
                        if (i == inboxResponse.size) {
                            showToast("Inbox Download Complete")
                        }
                        vm.downloadPdf(
                            context = requireContext(),
                            url = inbox.DocumentUrl,
                            title = inbox.DateTitle,
                            documentKey = inbox.DocumentKey
                        )
                    }
                }
            }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            sp.save("inboxDownload", true)
            sp.save("inboxDownloadC", "No")
            dialog.dismiss()
        }
        builder.show()
    }

    //Toast
    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(fragContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    //Debug Menu
    private fun debugMenu() {
        val sp = SharedPreference(requireContext())
        val urlDb = UrlsDatabaseBuilder.getInstance(requireContext()).UrlDao()
        (urlList as MutableList<UrlsData>).reverse()
        binding.btnUrlDebug.setOnClickListener {
            binding.llLogin2.isVisible = false
            binding.clDebug.isVisible = true
        }
        adapter = BaseUrlAdapter(requireContext(), urlList)
        binding.rvBaseUrls.adapter = adapter
        adapter.setOnUrlClick(object : BaseUrlAdapter.OnUrlClick {
            override fun onUrlClick(position: Int) {
                urlList[position]
                binding.tvUrlDebug.text = getString(R.string.url, sp.getValueString("baseUrl"))
            }
        })
        binding.rvBaseUrls.layoutManager = LinearLayoutManager(requireContext())
        binding.tvUrlDebug.text = getString(R.string.url, sp.getValueString("baseUrl"))
        binding.etBase.setText(sp.getValueString("baseUrl"))
        binding.etBase.addTextChangedListener {
            if (binding.etBase.text.toString().isEmpty()) {
                "Url : " + sp.getValueString("baseUrl").toString()
            }
        }
        binding.btnConfirmUrl.setOnClickListener {
            if (binding.etBase.text!!.isEmpty()) {
                Toast.makeText(requireContext(), "Text is empty", Toast.LENGTH_SHORT).show()
            } else {
                val url = binding.etBase.text.toString()
                sp.save("baseUrl", url)
                runBlocking { urlDb.insertUrl(UrlsData(baseUrl = url)) }
                urlList = runBlocking {
                    UrlsDatabaseBuilder.getInstance(requireContext()).UrlDao().getAllBaseUrls()
                }
                (urlList as MutableList<UrlsData>).reverse()
                adapter = BaseUrlAdapter(requireContext(), urlList)
                binding.rvBaseUrls.adapter = adapter
                binding.rvBaseUrls.layoutManager = LinearLayoutManager(requireContext())
                adapter.setOnUrlClick(object : BaseUrlAdapter.OnUrlClick {
                    override fun onUrlClick(position: Int) {
                        val currentUrl = urlList[position]
                        sp.save("baseUrl", currentUrl.baseUrl)
                        binding.tvUrlDebug.text =
                            getString(R.string.url, sp.getValueString("baseUrl").toString())
                    }
                })
                binding.tvUrlDebug.text =
                    getString(R.string.url, sp.getValueString("baseUrl").toString())
            }
        }
        binding.btnClear.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                urlDb.deleteAllUrls()
            }
            (urlList as MutableList<UrlsData>).clear()
            adapter = BaseUrlAdapter(requireContext(), urlList)
            binding.rvBaseUrls.adapter = adapter
            binding.rvBaseUrls.layoutManager = LinearLayoutManager(requireContext())
            adapter.setOnUrlClick(object : BaseUrlAdapter.OnUrlClick {
                override fun onUrlClick(position: Int) {
                    val currentUrl = urlList[position]
                    sp.save("baseUrl", currentUrl.baseUrl)
                    binding.tvUrlDebug.text =
                        getString(R.string.url, sp.getValueString("baseUrl").toString())
                }
            })
            SharedPreference(requireContext()).save("baseUrl", Urls(requireContext()).baseUrl)
            binding.tvUrlDebug.text =
                getString(R.string.url, sp.getValueString("baseUrl").toString())
            binding.etBase.text!!.clear()
        }
        binding.btnCloseDebug.setOnClickListener {
            binding.llLogin2.isVisible = true
            binding.clDebug.isVisible = false
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
        }
        adapter.setOnUrlClick(object : BaseUrlAdapter.OnUrlClick {
            override fun onUrlClick(position: Int) {
                val currentUrl = urlList[position]
                sp.save("baseUrl", currentUrl.baseUrl)
                binding.tvUrlDebug.text =
                    getString(R.string.url, sp.getValueString("baseUrl").toString())
            }
        })
    }
}