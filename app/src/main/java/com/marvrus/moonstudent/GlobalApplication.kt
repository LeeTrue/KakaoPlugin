package com.marvrus.moonstudent

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "5ccfcb8f6649a06fddcd162528b6f908")

        /** Naver Login Module Initialize */
        val naverClientId = "I1IDJscwHE5y4xpx6T6L"
        val naverClientSecret = "LA13o4g_6i"
        val naverClientName = "MEEMZ"

        NaverIdLoginSDK.initialize(this, naverClientId, naverClientSecret , naverClientName)
    }
}