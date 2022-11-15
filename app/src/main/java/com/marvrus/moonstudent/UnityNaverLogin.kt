package com.marvrus.moonstudent

import android.util.Log
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.NidOAuthLoginState
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import com.unity3d.player.UnityPlayer

class UnityNaverLogin {
    val context = UnityPlayer.currentActivity

    var naverToken: String? = ""
    val profileCallback = object : NidProfileCallback<NidProfileResponse> {
        override fun onSuccess(response: NidProfileResponse) {
            val userId = response.profile?.id
            val result = "id: ${userId}, token: ${naverToken}"
            Log.i("Unity", "네이버 아이디 로그인 성공! ${result}")

            val UserInfo =
                String.format("{\"accessToken\":\"%s\", \"userId\":\"%s\"}", naverToken, userId)
            UnityPlayer.UnitySendMessage("Login(Clone)", "NaverLoginAuthCallback", UserInfo)
        }

        override fun onFailure(httpStatus: Int, message: String) {
            val errorCode = NaverIdLoginSDK.getLastErrorCode().code
            val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
            //Log.e("Unity", "errorCode: ${errorCode}, errorDescription: ${errorDescription}")
            var _error =
                "사용자 정보 요청 실패 :: " + "errorCode: ${errorCode}, errorDescription: ${errorDescription}"
            UnityPlayer.UnitySendMessage("Login(Clone)", "NaverLoginFailedCallback", _error)
        }

        override fun onError(errorCode: Int, message: String) {
            onFailure(errorCode, message)
        }
    }

    /** OAuthLoginCallback을 authenticate() 메서드 호출 시 파라미터로 전달하거나 NidOAuthLoginButton 객체에 등록하면 인증이 종료되는 것을 확인할 수 있습니다. */
    val oauthLoginCallback = object : OAuthLoginCallback {
        override fun onSuccess() {
            // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
            naverToken = NaverIdLoginSDK.getAccessToken()
            //var naverRefreshToken = NaverIdLoginSDK.getRefreshToken()
            //var naverExpiresAt = NaverIdLoginSDK.getExpiresAt().toString()
            //var naverTokenType = NaverIdLoginSDK.getTokenType()
            //var naverState = NaverIdLoginSDK.getState().toString()

            //Log.i("Unity", "naverToken : ${naverToken}")
            //로그인 유저 정보 가져오기
            NidOAuthLogin().callProfileApi(profileCallback)
        }

        override fun onFailure(httpStatus: Int, message: String) {
            val errorCode = NaverIdLoginSDK.getLastErrorCode().code
            val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
            //Log.e("Unity", "errorCode: ${errorCode}, errorDescription: ${errorDescription}")
            var _error =
                "사용자 정보 요청 실패 :: " + "errorCode: ${errorCode}, errorDescription: ${errorDescription}"
            UnityPlayer.UnitySendMessage("Login(Clone)", "NaverLoginFailedCallback", _error)
        }

        override fun onError(errorCode: Int, message: String) {
            onFailure(errorCode, message)
        }
    }

    /**
     * 로그인
     * authenticate() 메서드를 이용한 로그인 **/
    fun NaverLogin() {
        Log.i("Unity", "NaverIdLoginSDK.getState() :: ${NaverIdLoginSDK.getState()}")
        // 네이버 로그인 중간에 취소한 경우, 재로그인 시도하는 경우
        if (NidOAuthLoginState.OK.equals(NaverIdLoginSDK.getState())) {
            RefreshAccessToken()
        } else {
            NaverIdLoginSDK.authenticate(context, oauthLoginCallback)
        }
    }

    /**
     * 로그인 버튼을 연속으로 누르는 경우 바로 authenticate()를 호출하지않고 토큰 갱신한 후, 호출 **/
    fun RefreshAccessToken(){
        NidOAuthLogin().callRefreshAccessTokenApi(context, object : OAuthLoginCallback {
            override fun onSuccess() {
                // 접근 토큰 갱신에 성공한 경우 수행할 코드 추가
                Log.i("Unity", "접근 토큰 갱신에 성공!")

                NaverIdLoginSDK.authenticate(context, oauthLoginCallback)
            }
            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.e("Unity", "\"접근 토큰 갱신 실패 :: \" + \"errorCode: ${errorCode}, errorDescription: ${errorDescription}\"")

                // 사용자가 네이버의 내정보 > 보안설정 > 외부 사이트 연결 페이지에서 연동을 해제한 경우, 토큰 삭제하고 재로그인 시도.
                DeleteAccessToken()
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        })
    }

    /**
     * 사용자가 연동을 해제한 경우, 토큰을 삭제한 후, authenticate() 함수 호출. **/
    fun DeleteAccessToken(){
        NidOAuthLogin().callDeleteTokenApi(context, object : OAuthLoginCallback {
            override fun onSuccess() {
                //서버에서 토큰 삭제에 성공한 상태입니다.
                Log.i("Unity", "서버에서 토큰 삭제에 성공!")

                NaverIdLoginSDK.authenticate(context, oauthLoginCallback)
            }
            override fun onFailure(httpStatus: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                //Log.e("Unity", "errorCode: ${errorCode}, errorDescription: ${errorDescription}")
                var _error =
                    "서버에서 토큰 삭제 실패 :: " + "errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}, " +
                            "errorDescription: ${NaverIdLoginSDK.getLastErrorDescription()}"
                UnityPlayer.UnitySendMessage("Login(Clone)", "NaverLoginFailedCallback", _error)
            }
            override fun onError(errorCode: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                onFailure(errorCode, message)
            }
        })
    }

    /**
     * 로그아웃
     * 애플리케이션에서 로그아웃할 때는 다음과 같이 NaverIdLoginSDK.logout() 메서드를 호출합니다. **/
    fun NaverLogout(){
        NaverIdLoginSDK.logout()
        Log.i("Unity", "네이버 아이디 로그아웃 성공!")
    }

    /**
     * 회원탈퇴
     * 사용자가 회원 탈퇴를 한 경우 토큰을 삭제 **/
    fun NaverSecession(){
        NidOAuthLogin().callDeleteTokenApi(context, object : OAuthLoginCallback {
            override fun onSuccess() {
                //서버에서 토큰 삭제에 성공한 상태입니다.
                Log.i("Unity", "서버에서 토큰 삭제에 성공!")
            }
            override fun onFailure(httpStatus: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                Log.e("Unity", "서버에서 토큰 삭제 실패 :: " + "errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}, " +
                                            "errorDescription: ${NaverIdLoginSDK.getLastErrorDescription()}")
            }
            override fun onError(errorCode: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                onFailure(errorCode, message)
            }
        })
    }
}