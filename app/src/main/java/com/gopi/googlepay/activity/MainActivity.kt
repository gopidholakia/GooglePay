package com.gopi.googlepay.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.gopi.googlepay.R
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import com.gopi.googlepay.adapter.ItemAdapter
import com.gopi.googlepay.model.ItemInfo
import com.gopi.googlepay.utils.PaymentsUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private val itemList = ArrayList<ItemInfo>()
    private var mAdapter: ItemAdapter? = null
    private var mPaymentsClient: PaymentsClient? = null
    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
    var isGPayAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPaymentsClient = PaymentsUtil.createPaymentsClient(this)

        mAdapter = ItemAdapter(itemList, this@MainActivity)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        rvItems.layoutManager = mLayoutManager
        rvItems.itemAnimator = DefaultItemAnimator()
        rvItems.adapter = mAdapter

        prepareData()
    }

    private fun prepareData() {
        itemList.add(ItemInfo("Nikon COOLPIX P1000 Digital Camera", 100, R.drawable.camera2))
        itemList.add(ItemInfo("Sony Cyber-shot DSC-RX10 IV", 100, R.drawable.camera1))
        itemList.add(ItemInfo("Nikon D5600 DSLR Camera", 100, R.drawable.camera3))
        mAdapter?.notifyDataSetChanged()
    }

    fun requestPayment(item: ItemInfo) {

        val price = PaymentsUtil.microsToString(item.priceMicros)
        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(price)
        if (!paymentDataRequestJson.isPresent) {
            return
        }
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString())

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        if (request != null) {
            AutoResolveHelper.resolveTask<PaymentData>(
                    mPaymentsClient!!.loadPaymentData(request), this, LOAD_PAYMENT_DATA_REQUEST_CODE)
        }
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            // value passed in AutoResolveHelper
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val paymentData = PaymentData.getFromIntent(data)
                        handlePaymentSuccess(paymentData!!)
                    }
                    Activity.RESULT_CANCELED -> {
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        val status = AutoResolveHelper.getStatusFromIntent(data)
                        handleError(status!!.statusCode)
                    }
                }
            }
        }
    }


    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson() ?: return

        // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
        val paymentMethodData: JSONObject

        try {
            paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".
            if (paymentMethodData
                            .getJSONObject("tokenizationData")
                            .getString("type") == "PAYMENT_GATEWAY" && paymentMethodData
                            .getJSONObject("tokenizationData")
                            .getString("token") == "examplePaymentMethodToken") {
                val alertDialog = AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage(
                                "Gateway name set to \"example\" - please modify " + "Constants.java and replace it with your own gateway.")
                        .setPositiveButton("OK", null)
                        .create()
                alertDialog.show()
            }

            val billingName = paymentMethodData.getJSONObject("info").getJSONObject("billingAddress").getString("name")
            Log.d("BillingName", billingName)
            Toast.makeText(this, getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG)
                    .show()

            // Logging token string.

            Log.d("GooglePaymentToken", paymentMethodData.getJSONObject("tokenizationData").getString("token"))
        } catch (e: JSONException) {
            Log.e("handlePaymentSuccess", "Error: " + e.toString())
            return
        }

    }

    private fun handleError(statusCode: Int) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode))
    }

    private fun possiblyShowGooglePayButton() {
        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest
        if (!isReadyToPayJson.isPresent) {
            return
        }
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString()) ?: return

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task = mPaymentsClient?.isReadyToPay(request!!)
        task?.addOnCompleteListener { task ->
            try {
                val result = task.getResult(ApiException::class.java!!)!!
                isGPayAvailable = result
            } catch (exception: ApiException) {
                // Process error
                Log.w("isReadyToPay failed", exception)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        possiblyShowGooglePayButton()
    }
}
