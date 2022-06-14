package com.ssafy.smartstore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.Toast
import com.ssafy.smartstore.database.UserDTO
import com.ssafy.smartstore.databinding.ActivityJoinBinding
import com.ssafy.smartstore.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.CacheResponse
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response

// F02: 회원 관리 - 회원 정보 추가 회원 가입 - 회원 정보를 추가할 수 있다.
// F03: 회원 관리 - 회원 아이디 중복 확인 - 회원 가입 시 아이디가 중복되는지 여부를 확인할 수 있다.

private const val TAG = "JoinActivity_싸피"
class JoinActivity : AppCompatActivity() {

    private lateinit var binding : ActivityJoinBinding
    private var idCheck = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var newId = binding.joinID.text.toString()
        var newPass = binding.joinPW.text.toString()
        var newNickname = binding.joinNickname.text.toString()
        idCheck = false

        binding.btnCheck.setOnClickListener {
            Log.d(TAG, "onCreate: 아이디 중복 확인 버튼 클릭")
            newId = binding.joinID.text.toString()
            newPass = binding.joinPW.text.toString()
            newNickname = binding.joinNickname.text.toString()
            //Log.d(TAG, "onCreate: $newId")
            //1. 아이디 중복 여부 확인
            checkId(newId)
        }

        binding.btnJoin.setOnClickListener {
            newId = binding.joinID.text.toString()
            newPass = binding.joinPW.text.toString()
            newNickname = binding.joinNickname.text.toString()
            if(binding.joinNickname.text.isBlank()||binding.joinPW.text.isBlank()){
                Toast.makeText(this@JoinActivity,"빈 칸을 모두 채워주세요.", Toast.LENGTH_SHORT).show()
            }
            else if(

                binding.joinNickname.text.isNotBlank()&&binding.joinPW.text.isNotBlank()&&idCheck){
                //회원가입
                CoroutineScope(Dispatchers.Main).launch {
                    //MobileCafeRepository.get().userInsert(UserDTO(newId, newNickname,newPass,0))
                    val service = MobileCafeApplication.retrofit.create(UserService::class.java)
                    service.userInsert(UserDTO(newId, newNickname, newPass, 0,)).enqueue(object: Callback<Unit>{
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            //정상일 경우 가져옴
                            if (response.code() == 200) {
                                var check = response.body()!!
                                Log.d(TAG, "onResponse: ${check}")
                                Toast.makeText(this@JoinActivity,"회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                            else {
                                Log.d(TAG, "회원가입 오류 - onResponse : Error code ${response.code()}")
                            }

                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            t.printStackTrace()
                            Log.d(TAG, "onFailure: 회원 가입 오류")

                        }

                    })

                }
                binding.joinID.setText("")
                binding.joinPW.setText("")
                binding.joinNickname.setText("")

                finish()
            }
            else{
                Toast.makeText(this@JoinActivity,"아이디 중복 여부를 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkId(id: String){

        CoroutineScope(Dispatchers.Main).launch {
            //회원 테이블에서 같은 아이디가 있는지 확인한다
            val service = MobileCafeApplication.retrofit.create(UserService::class.java)
            service.checkUser(id).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.code() == 200) {
                        val res = response.body()!!
                        if(!res){
                            Toast.makeText(this@JoinActivity,"사용 가능한 아이디입니다.",Toast.LENGTH_SHORT).show()
                            idCheck=true
                            binding.btnCheck.setImageResource(R.drawable.check)
                        }
                        else{
                            Toast.makeText(this@JoinActivity, "중복되는 아이디입니다.",Toast.LENGTH_SHORT).show()
                            idCheck=false
                            binding.btnCheck.setImageResource(R.drawable.check_mark)
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
    }
}

