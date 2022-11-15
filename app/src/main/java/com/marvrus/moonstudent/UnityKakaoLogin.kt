package com.marvrus.moonstudent

import android.util.Log
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import com.unity3d.player.UnityPlayer

class UnityKakaoLogin {

    val context = UnityPlayer.currentActivity
    /*fun kakaoLogin() {
        // 카카오 로그인 토큰 존재 여부 확인하기
        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error != null)
                {
                    if (error is KakaoSdkError && error.isInvalidTokenError() == true)
                    {
                        // 토큰 에러가 있을 경우, 카카오 로그인
                        Log.e("Unity", "토큰 카카오 SDK 에러")
                        getKakaoLoginData()
                    }
                    else
                    {
                        //기타 에러
                        var _error = "카카오 로그인 토큰 오류 :: " + error.toString()
                        UnityPlayer.UnitySendMessage("Login(Clone)", "KakaoLoginFailedCallback", _error)
                    }
                }
                else
                {
                    Log.i("Unity", "토큰 유효성 체크 성공(필요 시 토큰 갱신됨)")
                    val UserInfo = String.format("{\"isFirst\": \"false\"")
                    UnityPlayer.UnitySendMessage("Login(Clone)", "KakaoLoginAuthCallback", UserInfo)
                }
            }
        }
        else
        {
            Log.i("Unity", "토큰이 존재하지 않음")
            // 카카오 로그인 토큰이 존재하지 않을 경우, 카카오 로그인
            getKakaoLoginData()
        }
    }*/

    // 카카오 로그인 이후, AccessToken, UserId 받아오기
    fun kakaoLogin()
    {
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null)
            {
                //Log.e("Unity", "카카오계정으로 로그인 실패", error)
                var _error = "카카오계정으로 로그인 실패 :: " + error.toString()
                UnityPlayer.UnitySendMessage("Login(Clone)", "KakaoLoginFailedCallback", _error)
            }
            else if (token != null)
            {
                Log.i("Unity", "카카오계정으로 로그인 성공 ${token.accessToken}")

                // 사용자 정보 요청 (기본)
                UserApiClient.instance.me { user, error ->
                    if (error != null)
                    {
                        //Log.e("Unity", "사용자 정보 요청 실패", error)
                        var _error = "사용자 정보 요청 실패 :: " + error.toString()
                        UnityPlayer.UnitySendMessage("Login(Clone)", "KakaoLoginFailedCallback", _error)
                    }
                    else if (user != null)
                    {
                        Log.i("Unity", "사용자 정보 요청 성공 :: " + "회원번호: ${user.id}" +
                                "이메일: ${user.kakaoAccount?.email}" + "닉네임: ${user.kakaoAccount?.profile?.nickname}")
                        val UserInfo = String.format("{\"accessToken\":\"%s\", \"userId\":\"%s\"}", token.accessToken, user.id)
                        UnityPlayer.UnitySendMessage("Login(Clone)", "KakaoLoginAuthCallback", UserInfo)
                    }
                }
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null)
                {
                    Log.e("Unity", "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                }
                else if (token != null)
                {
                    Log.i("Unity", "카카오톡으로 로그인 성공 accessToken :: ${token.accessToken}")

                    // 사용자 정보 요청 (기본)
                    UserApiClient.instance.me { user, error ->
                        if (error != null)
                        {
                            //Log.e("Unity", "사용자 정보 요청 실패", error)
                            var _error = "사용자 정보 요청 실패 :: " + error.toString()
                            UnityPlayer.UnitySendMessage("Login(Clone)", "KakaoLoginFailedCallback", _error)
                        }
                        else if (user != null)
                        {
                            Log.i("Unity", "사용자 정보 요청 성공 :: " + "회원번호: ${user.id}" +
                                    "이메일: ${user.kakaoAccount?.email}" + "닉네임: ${user.kakaoAccount?.profile?.nickname}")
                            val UserInfo = String.format("{\"accessToken\":\"%s\", \"userId\":\"%s\"}", token.accessToken, user.id)
                            UnityPlayer.UnitySendMessage("Login(Clone)", "KakaoLoginAuthCallback", UserInfo)
                        }
                    }
                }
            }
        }
        else
        {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    // 카카오 로그아웃
    fun kakaoLogout()
    {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e("Unity", "로그아웃 실패. SDK에서 토큰 삭제됨", error)
            }
            else {
                Log.i("Unity", "로그아웃 성공. SDK에서 토큰 삭제됨")
            }
        }
    }
}