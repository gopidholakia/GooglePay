

package com.gopi.googlepay.utils

import android.app.Activity

import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import java8.util.Optional

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.math.BigDecimal
import java.math.RoundingMode


object PaymentsUtil {
    private val MICROS = BigDecimal(1000000.0)

    private val baseRequest: JSONObject
        @Throws(JSONException::class)
        get() = JSONObject().put("apiVersion", 2).put("apiVersionMinor", 0)

    private val gatewayTokenizationSpecification: JSONObject
        @Throws(JSONException::class, RuntimeException::class)
        get() {
            if (Constants.PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS.isEmpty()) {
                throw RuntimeException(
                        "Please edit the Constants.java file to add gateway name and other parameters your " + "processor requires")
            }
            val tokenizationSpecification = JSONObject()

            tokenizationSpecification.put("type", "PAYMENT_GATEWAY")
            val parameters = JSONObject(Constants.PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS)
            tokenizationSpecification.put("parameters", parameters)

            return tokenizationSpecification
        }

    private val allowedCardNetworks: JSONArray
        get() = JSONArray(Constants.SUPPORTED_NETWORKS)

    private val allowedCardAuthMethods: JSONArray
        get() = JSONArray(Constants.SUPPORTED_METHODS)


    private// Optionally, you can add billing address/phone number associated with a CARD payment method.
    val baseCardPaymentMethod: JSONObject
        @Throws(JSONException::class)
        get() {
            val cardPaymentMethod = JSONObject()
            cardPaymentMethod.put("type", "CARD")

            val parameters = JSONObject()
            parameters.put("allowedAuthMethods", allowedCardAuthMethods)
            parameters.put("allowedCardNetworks", allowedCardNetworks)
            parameters.put("billingAddressRequired", true)

            val billingAddressParameters = JSONObject()
            billingAddressParameters.put("format", "FULL")

            parameters.put("billingAddressParameters", billingAddressParameters)

            cardPaymentMethod.put("parameters", parameters)

            return cardPaymentMethod
        }


    private val cardPaymentMethod: JSONObject
        @Throws(JSONException::class)
        get() {
            val cardPaymentMethod = baseCardPaymentMethod
            cardPaymentMethod.put("tokenizationSpecification", gatewayTokenizationSpecification)

            return cardPaymentMethod
        }


    val isReadyToPayRequest: Optional<JSONObject>
        get() {
            try {
                val isReadyToPayRequest = baseRequest
                isReadyToPayRequest.put(
                        "allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod))

                return Optional.of(isReadyToPayRequest)
            } catch (e: JSONException) {
                return Optional.empty()
            }

        }


    private val merchantInfo: JSONObject
        @Throws(JSONException::class)
        get() = JSONObject().put("merchantName", "Example Merchant")

    fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder().setEnvironment(Constants.PAYMENTS_ENVIRONMENT).build()
        return Wallet.getPaymentsClient(activity, walletOptions)
    }


    @Throws(JSONException::class)
    private fun getTransactionInfo(price: String): JSONObject {
        val transactionInfo = JSONObject()
        transactionInfo.put("totalPrice", price)
        transactionInfo.put("totalPriceStatus", "FINAL")
        transactionInfo.put("currencyCode", Constants.CURRENCY_CODE)

        return transactionInfo
    }


    fun getPaymentDataRequest(price: String): Optional<JSONObject> {
        try {
            val paymentDataRequest = PaymentsUtil.baseRequest
            paymentDataRequest.put(
                    "allowedPaymentMethods", JSONArray().put(PaymentsUtil.cardPaymentMethod))
            paymentDataRequest.put("transactionInfo", PaymentsUtil.getTransactionInfo(price))
            paymentDataRequest.put("merchantInfo", PaymentsUtil.merchantInfo)


            paymentDataRequest.put("shippingAddressRequired", false)
            val shippingAddressParameters = JSONObject()
            shippingAddressParameters.put("phoneNumberRequired", false)

            val allowedCountryCodes = JSONArray(Constants.SHIPPING_SUPPORTED_COUNTRIES)

            shippingAddressParameters.put("allowedCountryCodes", allowedCountryCodes)
            paymentDataRequest.put("shippingAddressParameters", shippingAddressParameters)
            return Optional.of(paymentDataRequest)
        } catch (e: JSONException) {
            return Optional.empty()
        }

    }


    fun microsToString(micros: Long): String {
        return BigDecimal(micros).divide(MICROS).setScale(2, RoundingMode.HALF_EVEN).toString()
    }
}
