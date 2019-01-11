/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gopi.googlepay.utils

import com.google.android.gms.wallet.WalletConstants

import java.util.Arrays
import java.util.HashMap


object Constants {

    val PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST
    val SUPPORTED_NETWORKS = Arrays.asList(
            "AMEX",
            "DISCOVER",
            "JCB",
            "MASTERCARD",
            "VISA")
    val SUPPORTED_METHODS = Arrays.asList(
            "PAN_ONLY",
            "CRYPTOGRAM_3DS")
    val CURRENCY_CODE = "USD"
    val SHIPPING_SUPPORTED_COUNTRIES = Arrays.asList("US", "GB")
    val PAYMENT_GATEWAY_TOKENIZATION_NAME = "example"
    val PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS: HashMap<String, String> = object : HashMap<String, String>() {
        init {
            put("gateway", PAYMENT_GATEWAY_TOKENIZATION_NAME)
            put("gatewayMerchantId", "exampleGatewayMerchantId")
            // Your processor may require additional parameters.
        }
    }
    val DIRECT_TOKENIZATION_PUBLIC_KEY = "REPLACE_ME"

    val DIRECT_TOKENIZATION_PARAMETERS: HashMap<String, String> = object : HashMap<String, String>() {
        init {
            put("protocolVersion", "ECv1")
            put("publicKey", DIRECT_TOKENIZATION_PUBLIC_KEY)
        }
    }
}
