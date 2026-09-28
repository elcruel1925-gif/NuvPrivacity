package com.nuvprivacity.app

import android.app.Activity
import android.webkit.JavascriptInterface
import com.android.billingclient.api.*
import org.json.JSONObject

class NuvBillingBridge(
    private val activity: Activity,
    private val webView: android.webkit.WebView
) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_ID = "nuvprivacity_premium"
        const val MONTHLY_BASE_PLAN = "monthly"
        const val ANNUAL_BASE_PLAN = "annual"
    }

    private var billingClient: BillingClient = BillingClient.newBuilder(activity)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private var productDetails: ProductDetails? = null

    init { connect() }

    private fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProduct()
                    queryPurchases()
                } else {
                    send("npBillingStatus", JSONObject().put("ready", false).put("code", result.responseCode).put("message", result.debugMessage))
                }
            }
            override fun onBillingServiceDisconnected() {
                send("npBillingStatus", JSONObject().put("ready", false).put("message", "Billing no disponible temporalmente"))
            }
        })
    }

    private fun queryProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            ).build()
        billingClient.queryProductDetailsAsync(params) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK && details.isNotEmpty()) {
                productDetails = details.first()
                val plans = JSONArrayBuilder()
                details.first().subscriptionOfferDetails.orEmpty().forEach { offer ->
                    val phase = offer.pricingPhases.pricingPhaseList.firstOrNull()
                    plans.add(JSONObject()
                        .put("basePlanId", offer.basePlanId)
                        .put("offerToken", offer.offerToken)
                        .put("formattedPrice", phase?.formattedPrice ?: "")
                        .put("period", phase?.billingPeriod ?: ""))
                }
                send("npBillingPlans", JSONObject().put("productId", PRODUCT_ID).put("plans", plans.toJsonArray()))
            } else {
                send("npBillingPlans", JSONObject().put("productId", PRODUCT_ID).put("plans", org.json.JSONArray()))
            }
        }
    }

    @JavascriptInterface
    fun buy(basePlanId: String) {
        activity.runOnUiThread {
            val details = productDetails
            if (details == null) {
                send("npBillingError", JSONObject().put("message", "El producto Premium todavía no está disponible en Google Play."))
                queryProduct()
                return@runOnUiThread
            }
            val offer = details.subscriptionOfferDetails.orEmpty().firstOrNull { it.basePlanId == basePlanId }
            if (offer == null) {
                send("npBillingError", JSONObject().put("message", "El plan seleccionado todavía no está configurado en Google Play."))
                return@runOnUiThread
            }
            val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offer.offerToken)
                .build()
            val flow = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParams))
                .build()
            billingClient.launchBillingFlow(activity, flow)
        }
    }

    @JavascriptInterface
    fun restore() { queryPurchases() }

    private fun queryPurchases() {
        if (!billingClient.isReady) return
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryPurchasesAsync
            var active = false
            purchases.forEach { purchase ->
                if (purchase.products.contains(PRODUCT_ID) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    active = true
                    if (!purchase.isAcknowledged) {
                        billingClient.acknowledgePurchase(
                            AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken).build()
                        ) {}
                    }
                }
            }
            send("npBillingEntitlement", JSONObject().put("premium", active))
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (purchase.products.contains(PRODUCT_ID) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged) {
                        billingClient.acknowledgePurchase(
                            AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken).build()
                        ) {}
                    }
                }
            }
            queryPurchases()
        } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            send("npBillingError", JSONObject().put("message", result.debugMessage))
        }
    }

    private fun send(function: String, payload: JSONObject) {
        val js = "window.$function && window.$function(${JSONObject.quote(payload.toString())})"
        webView.post { webView.evaluateJavascript(js, null) }
    }

    private class JSONArrayBuilder {
        private val arr = org.json.JSONArray()
        fun add(o: JSONObject) { arr.put(o) }
        fun toJsonArray() = arr
    }
}
