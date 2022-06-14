package com.ssafy.smartstore

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.ssafy.smartstore.database.OrderDetailForShoppingList
import com.ssafy.smartstore.database.UserDTO
import com.ssafy.smartstore.databinding.ActivityLoginBinding
import com.ssafy.smartstore.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


private const val TAG = "LoginActivity_싸피"
class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    // firebase authentication 관련
    private val RC_SIGN_IN: Int = 9001
    var mGoogleSignInClient: GoogleSignInClient? = null
    private var idCheck = false

    //페북 로그인 관련
    private var auth:FirebaseAuth? = null
    val FACEBOOK_REQUEST_CODE = 99
    var callbackManager:CallbackManager?=null


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //아이디랑 비번 있으면 그값으로 설정하고 home으로 넘어가기
        var id = MobileCafeApplication.prefs.getString("id", "")
        var pw = MobileCafeApplication.prefs.getString("pw", "")
        if (id != "" && pw != "") {
            //아이디랑 비번 있는지 확인
            println("앱 시작시 autoLogin")
            autoLogin(id, pw)
        }

        //로그인하고 sharedPreference에 로그인 값 저장
        binding.btnLogin.setOnClickListener {
            var id = binding.editID.text.toString()
            var pw = binding.editPW.text.toString()

            saveLogin(id, pw)
        }

        //가입버튼
        binding.btnJoin.setOnClickListener{
            var intent = Intent(this,JoinActivity::class.java)
            startActivity(intent)
        }

        //카카오톡(카카오계정)로그인
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (error != null) {
                //Toast.makeText(this, "토큰 정보 보기 실패", Toast.LENGTH_SHORT).show()
            } else if (tokenInfo != null) {
                //Toast.makeText(this, "토큰 정보 보기 성공", Toast.LENGTH_SHORT).show()
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//                finish()
                UserApiClient.instance.me { user, error ->
                    var id = user?.kakaoAccount?.email.toString()
                    var pass = user?.id.toString()
                    //Log.d(TAG, "onCreate:  비밀번호: $pass")
                    autoLogin(id, pass)
                    //checkId(id)
                }
            }
        }

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Toast.makeText(this, "접근이 거부 됨(동의 취소)", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                        Toast.makeText(this, "유효하지 않은 앱", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                        Toast.makeText(this, "인증 수단이 유효하지 않아 인증할 수 없는 상태", Toast.LENGTH_SHORT)
                            .show()
                    }
                    error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                        Toast.makeText(this, "요청 파라미터 오류", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                        Toast.makeText(this, "유효하지 않은 scope ID", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                        Toast.makeText(this, "설정이 올바르지 않음(android key hash)", Toast.LENGTH_SHORT)
                            .show()
                    }
                    error.toString() == AuthErrorCause.ServerError.toString() -> {
                        Toast.makeText(this, "서버 내부 에러", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                        Toast.makeText(this, "앱이 요청 권한이 없음", Toast.LENGTH_SHORT).show()
                    }
                    else -> { // Unknown
                        Toast.makeText(this, "기타 에러", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (token != null) {

                //Toast.makeText(this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                CoroutineScope(Dispatchers.Main).launch {
                    UserApiClient.instance.me { user, error ->
                        if (user != null) {
                            var id = user?.kakaoAccount?.email.toString()
                            var nickName = user?.kakaoAccount?.profile?.nickname.toString()
                            var pass = user?.id.toString()
                            //Log.d(TAG, "onCreate:  비밀번호: $pass")
                            checkId(id,nickName,pass)
                            autoLogin(id,pass)
                            Log.d(TAG, "onCreate: 로그인 후아이디 중복 체크")

                        }
                    }

                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY))
                finish()
            }
        }

        binding.btnKakaoLogin.setOnClickListener {
                if(LoginClient.instance.isKakaoTalkLoginAvailable(this)){
                    LoginClient.instance.loginWithKakaoTalk(this, callback = callback)
                }else{
                    LoginClient.instance.loginWithKakaoAccount(this, callback = callback)
                }
        }

        // 구글로그인
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.btnGoogleLogin.setOnClickListener{
            signIn()
        }

        //
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create()

        auth = Firebase.auth
        binding.btnFacebookLogin.setReadPermissions("email", "public_profile")
        binding.btnFacebookLogin.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth!!.currentUser
//                    updateUI(user)
                    println("user : ${user!!.email} ${user!!.displayName} ${user!!.phoneNumber} ${user!!.uid}")
                    //디비에 값 저장되어있으면 패스

                    //디비에 값 저장 안되어있으면 저장하고 로그인
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
//                    updateUI(null)
                }
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }

        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            //아이디가 서버에 없으면 추가
            checkId(account.email.toString(),account.displayName.toString(), account.idToken?.substring(0,15).toString())
//            if(idCheck){
//                //비밀번호 null값 들어감
//                joinServer(account.email.toString(), account.displayName.toString(), account.idToken?.substring(0,15).toString())
//                //Log.d(TAG, "handleSignInResult: ${account.idToken}")
//            }
            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        println("송공?")
        println("email : ${account?.email}")
        println("name : ${account?.displayName}")

        //구글 로그인 되어있으면
        autoLogin(account?.email.toString(), account?.idToken?.substring(0,10).toString())
    }


    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun saveIDAndPW(id:String, pw:String, name:String, stamps:Int) {

        //아이디 비번 저장
        MobileCafeApplication.prefs.setString("id", id)
        MobileCafeApplication.prefs.setString("pw", pw)
        MobileCafeApplication.prefs.setString("name",name)
        MobileCafeApplication.prefs.setString("stamps",stamps.toString())//가져올때는 int로

        //가져올때
        //MobileCafeApplication.prefs.getString("id","")
        //MobileCafeApplication.prefs.getString("pw","")
    }

    private fun saveLogin(id:String, pw:String){
        //아이디랑 비번 있는지 확인
        CoroutineScope(Dispatchers.Main).launch {
            val service = MobileCafeApplication.retrofit.create(UserService::class.java)
            service.userLogin(UserDTO(id, "", pw, 0)).enqueue(object :
                Callback<UserDTO> {
                override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                    if (response.code() == 200) {
                        var check = response.body()!!
                        Log.d(TAG, "onResponse: ${check}")
                        //shared preference 저장 로직
                        saveIDAndPW(check.id, check.pass, check.name, check.stamps)
                        userId = check.id
                        userPW = check.pass
                        userName = check.name
                        userStamp = check.stamps


                        var intent = Intent(applicationContext, MainActivity::class.java)
                        //startActivity()
                        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))

                    } else {
                        Log.d(TAG, "로그인 오류 - onResponse : Error code ${response.code()}")

                    }
                }

                override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                    t.printStackTrace()
                    Log.d(TAG, "onFailure: 로그인 오류")
                    Toast.makeText(this@LoginActivity,"아이디와 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

    companion object {
        var userId = ""
        var userPW = ""
        var userName = ""
        var userStamp = 0
        var tmpOrderList2 = mutableListOf<OrderDetailForShoppingList>()

        var recent_order = -1 //가장 최근 주문한 내역 저장

        var current_lan = 0.0 //현재 위치 정보
        var current_lon = 0.0

        var notiList = mutableListOf<String>() //메시지 푸시 알림

        var store_lan = 36.10830144233874 // 매장 위치 정보
        var store_lon = 128.41827450414362

    }

    //아이디 중복체크
    private fun checkId(id: String,nickName:String, pw:String):Boolean{

        var answer = false
        Log.d(TAG, "checkId: 중복체크하는 아이디: $id")

        //토스트만 지우면 됌.
        CoroutineScope(Dispatchers.Main).launch {
            //회원 테이블에서 같은 아이디가 있는지 확인한다
            val service = MobileCafeApplication.retrofit.create(UserService::class.java)
            service.checkUser(id).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.code() == 200) {
                        val res = response.body()!!
                        if(!res){
                            //Toast.makeText(this@LoginActivity,"사용 가능한 아이디입니다.",Toast.LENGTH_SHORT).show()
                            idCheck=true
                            answer = true
                            Log.d(TAG, "onResponse: 사용가능한 아이디 $id")
                            joinServer(id,nickName,pw)
                        }
                        else{
                            //Toast.makeText(this@LoginActivity, "중복되는 아이디입니다.",Toast.LENGTH_SHORT).show()
                            idCheck=false
                            answer = false
                        }
                    }else{
                        Log.d(TAG, "onResponse: Error Code ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    t.printStackTrace()
                    Log.d(TAG, "onFailure: 아이디 중복 확인 오류")
                }

            }
            )
        }
        Log.d(TAG, "checkId: 중복체크 결과 ${answer}")
        return answer
    }

    //아이디 회원가입
    private fun joinServer(id:String, newNickname:String,pw:String){
        Log.d(TAG, "joinServer: 회원가입 id: $id")
            CoroutineScope(Dispatchers.Main).launch {
                //MobileCafeRepository.get().userInsert(UserDTO(newId, newNickname,newPass,0))
                val service = MobileCafeApplication.retrofit.create(UserService::class.java)
                service.userInsert(UserDTO(id, newNickname, pw, 0))
                    .enqueue(object : Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            //정상일 경우 가져옴
                            if (response.code() == 200) {
                                var check = response.body()!!
                                Log.d(TAG, "onResponse: ${check}")
                                Toast.makeText(
                                    this@LoginActivity,
                                    "가입 성공.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Log.d(TAG, "가입 오류 - onResponse : Error code ${response.code()}")
                            }

                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            t.printStackTrace()
                            Log.d(TAG, "onFailure:  가입 오류")

                        }

                    })
            }
    }

    //자동로그인
    private fun autoLogin(id:String,pw: String){
        println("자동 로그인 id : ${id}")
        CoroutineScope(Dispatchers.Main).launch {
            val service = MobileCafeApplication.retrofit.create(UserService::class.java)
            service.userLogin(UserDTO(id, "", pw, 0)).enqueue(object :
                Callback<UserDTO> {
                override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                    if (response.code() == 200) {
                        var check = response.body()!!
                        Log.d(TAG, "onResponse: ${check}")
                        //shared preference 저장 로직
                        saveIDAndPW(check.id, check.pass, check.name, check.stamps)
                        userId = check.id
                        userPW = check.pass
                        userName = check.name
                        userStamp = check.stamps


                        var intent = Intent(applicationContext, MainActivity::class.java)
                        //startActivity(intent)
                        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                        finish()

                    } else {
                        Log.d(TAG, "자동로그인 오류 - onResponse : Error code ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                    t.printStackTrace()
                    Log.d(TAG, "onFailure: 로그인 오류")
                    //Toast.makeText(this@LoginActivity,"자동로그인 오류 확인하세요.", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }
}